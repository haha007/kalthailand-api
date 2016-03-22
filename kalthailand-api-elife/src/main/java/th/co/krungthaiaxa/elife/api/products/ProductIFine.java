package th.co.krungthaiaxa.elife.api.products;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage;
import th.co.krungthaiaxa.elife.api.repository.ProductIFineRateRepository;

import javax.inject.Inject;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.*;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException.iFinePackageNameUnknown;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.*;

@Component
public class ProductIFine implements Product {
    public final static Integer DURATION_COVERAGE_IN_YEAR = 10;
    public final static Integer DURATION_PAYMENT_IN_YEAR = 10;
    public final static String PRODUCT_IFINE_NAME = "Product iFine";
    public final static String PRODUCT_IFINE_CURRENCY = "THB";
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 300000.0;
    public static final Double PREMIUM_MIN = null;
    public static final Double PREMIUM_MAX = null;
    public static final int MAX_AGE = 59;
    public static final int MIN_AGE = 18;

    @Inject
    private ProductIFineRateRepository productIFineRateRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IFINE_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(productQuotation)) {
            // we need to delete what might have been calculated before
            resetCalculatedStuff(quote, hasIFineCoverage);
            return;
        }

        Insured insured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // copy data already gathered in ProductQuotation
        Integer age = getAge(productQuotation.getDateOfBirth());
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        insured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        insured.setAgeAtSubscription(age);
        insured.getPerson().setGenderCode(productQuotation.getGenderCode());
        insured.setProfessionName(productQuotation.getOccupation());

        // cannot be too young or too old
        checkInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        insured.setStartDate(now(of(SHORT_IDS.get("VST"))));
        insured.setEndDate(insured.getStartDate().plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(insured.getStartDate().plusYears(DURATION_PAYMENT_IN_YEAR));

        // get iFine package from package name
        ProductIFinePackage productIFinePackage = getPackage(productQuotation.getPackageName());
        Double deathByAccident = productIFinePackage.getDeathByAccident();

        // set amounts
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        productIFinePremium.setSumInsured(amount(productIFinePackage.getSumInsured()));
        productIFinePremium.setAccidentSumInsured(amount(productIFinePackage.getAccidentSumInsured()));
        productIFinePremium.setHealthSumInsured(amount(productIFinePackage.getHealthSumInsured()));
        productIFinePremium.setHospitalizationSumInsured(amount(productIFinePackage.getHospitalizationSumInsured()));
        productIFinePremium.setDeathByAccident(amount(deathByAccident));
        productIFinePremium.setDeathByAccidentInPublicTransport(amount(deathByAccident * 2));
        productIFinePremium.setDisabilityFromAccidentMin(amount(deathByAccident * 2 / 100));
        productIFinePremium.setDisabilityFromAccidentMax(amount(deathByAccident));
        productIFinePremium.setLossOfHandOrLeg(amount(deathByAccident));
        productIFinePremium.setLossOfSight(amount(deathByAccident));
        productIFinePremium.setLossOfHearingMin(amount(deathByAccident * 15 / 100));
        productIFinePremium.setLossOfHearingMax(amount(deathByAccident * 75 / 100));
        productIFinePremium.setLossOfSpeech(amount(deathByAccident * 50 / 100));
        productIFinePremium.setLossOfCorneaForBothEyes(amount(deathByAccident * 50 / 100));
        productIFinePremium.setLossOfFingersMin(amount(deathByAccident * 2 / 100));
        productIFinePremium.setLossOfFingersMax(amount(deathByAccident * 70 / 100));
        productIFinePremium.setNoneCurableBoneFracture(amount(deathByAccident * 10 / 100));
        productIFinePremium.setLegsShortenBy5cm(amount(deathByAccident * 7.5 / 100));
        productIFinePremium.setBurnInjuryMin(amount(deathByAccident * 25 / 100));
        productIFinePremium.setBurnInjuryMax(amount(deathByAccident));
        productIFinePremium.setMedicalCareCost(amount(deathByAccident * 10 / 100));

        // calculates rates
        ProductIFineRate productIFineRate = productIFineRateRepository.findByPlanNameAndGender(productQuotation.getPackageName(), insured.getPerson().getGenderCode().name());
        Double taxDeductibleRate = productIFineRate.getTaxDeductibleRate().get(age - 18);
        Double nonTaxDeductibleRate = productIFineRate.getNonTaxDeductibleRate().get(age - 18);
        Double riskOccupationCharge = 0.0;
        if (productQuotation.getRiskOccupation()) {
            riskOccupationCharge = productIFineRate.getNonTaxDeductibleRiskRate().get(age - 18);
        }
        productIFinePremium.setBasicPremiumRate(taxDeductibleRate);
        productIFinePremium.setRiderPremiumRate(nonTaxDeductibleRate);
        productIFinePremium.setRiskOccupationCharge(riskOccupationCharge);

        // calculate premium
        Double factor = modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        Double taxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * taxDeductibleRate / 1000 * factor);
        Double nonTaxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * (nonTaxDeductibleRate + riskOccupationCharge) / 1000 * factor);
        productIFinePremium.setTaxDeductible(amount(taxDeductible));
        productIFinePremium.setNonTaxDeductible(amount(nonTaxDeductible));
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount(taxDeductible + nonTaxDeductible));
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(getCommonData());
        checkInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        checkInsuredAge(insured, MIN_AGE, MAX_AGE);
        checkMainInsured(insured);

        // Recalculate the quote
        calculateQuote(quote, null);

        // check for calculated data
        checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkIFinePremiumsData(quote.getPremiumsData());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add future payment schedule
        addPayments(policy, DURATION_PAYMENT_IN_YEAR);
    }

    @Override
    public CommonData getCommonData() {
        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_IFINE.getName());
        commonData.setProductName(PRODUCT_IFINE_NAME);
        commonData.setMaxAge(MAX_AGE);
        commonData.setMinAge(MIN_AGE);
        commonData.setMaxPremium(amount(PREMIUM_MAX));
        commonData.setMinPremium(amount(PREMIUM_MIN));
        commonData.setMaxSumInsured(amount(SUM_INSURED_MAX));
        commonData.setMinSumInsured(amount(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        return commonData;
    }

    @Override
    public ProductAmounts getProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(getCommonData());
        productAmounts.setMaxPremium(amount(PREMIUM_MAX));
        productAmounts.setMaxSumInsured(amount(SUM_INSURED_MAX));
        productAmounts.setMinPremium(amount(PREMIUM_MIN));
        productAmounts.setMinSumInsured(amount(SUM_INSURED_MIN));
        return productAmounts;
    }

    @Override
    public PremiumsData getPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIFinePremium productIFinePremium = new ProductIFinePremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIFinePremium(productIFinePremium);

        return premiumsData;
    }

    private ProductIFinePackage getPackage(String packageName) {
        ProductIFinePackage result = ProductIFinePackage.valueOf(packageName);
        notNull(result, iFinePackageNameUnknown);

        return result;
    }

    private boolean hasEnoughTocalculate(ProductQuotation productQuotation) {
        // Do we have a birth date to calculate the age of insured
        boolean hasAnyDateOfBirth = productQuotation.getDateOfBirth() != null;
        if (!hasAnyDateOfBirth) {
            return false;
        }

        // we need a gender
        boolean hasGender = productQuotation.getGenderCode() != null;
        if (!hasGender) {
            return false;
        }

        // we need an occupation
        boolean hasOccupation = productQuotation.getOccupation() != null;
        if (!hasOccupation) {
            return false;
        }

        // we need the product name
        boolean hasPackageName = productQuotation.getPackageName() != null;
        if (!hasPackageName) {
            return false;
        }

        // We need a periodicity
        return productQuotation.getPeriodicityCode() != null;
    }

    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        if (quote.getPremiumsData().getProductIFinePremium() != null) {
            quote.getPremiumsData().getProductIFinePremium().setNonTaxDeductible(null);
            quote.getPremiumsData().getProductIFinePremium().setTaxDeductible(null);
            quote.getPremiumsData().getProductIFinePremium().setBasicPremiumRate(null);
            quote.getPremiumsData().getProductIFinePremium().setProductIFinePackage(null);
            quote.getPremiumsData().getProductIFinePremium().setRiderPremiumRate(null);
            quote.getPremiumsData().getProductIFinePremium().setRiskOccupationCharge(null);
            quote.getPremiumsData().getProductIFinePremium().setSumInsured(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    private static void checkIFinePremiumsData(PremiumsData premiumsData) {
        notNull(premiumsData, premiumnsDataNone);
        notNull(premiumsData.getProductIFinePremium(), premiumnsDataNone);
        notNull(premiumsData.getProductIFinePremium().getSumInsured(), premiumnsDataNoSumInsured);
        notNull(premiumsData.getProductIFinePremium().getSumInsured().getCurrencyCode(), premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProductIFinePremium().getSumInsured().getValue(), premiumnsSumInsuredNoAmount);
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), PRODUCT_IFINE.getName(), productIFineExpected);
        isEqual(commonData.getProductName(), PRODUCT_IFINE_NAME, productIFineExpected);
    }

    private Double get2DigitsDouble(Double value) {
        return (double) Math.round(value * 100) / 100;
    }

    private Amount amount(Double value) {
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_IFINE_CURRENCY);
        amount.setValue(value);
        return amount;
    }
}
