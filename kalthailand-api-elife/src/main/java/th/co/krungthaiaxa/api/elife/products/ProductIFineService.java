package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIFinePremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.repository.ProductIFineRateRepository;

import javax.inject.Inject;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amountTHB;

@Component
public class ProductIFineService implements ProductService {
    public final static Integer DURATION_COVERAGE_IN_YEAR = 10;
    public final static Integer DURATION_PAYMENT_IN_YEAR = 10;
    public final static String PRODUCT_IFINE_NAME = "Product iFine";
    public final static String CURRENCY = "THB";
    /**
     * For understanding "sum insured" & "sum assured", please read here: http://www.livemint.com/Money/Ksp8iZvMDxOm9wdYSXuwnK/Did-you-know--What8217s-the-difference-between-sum-assur.html
     */
    public static final Double SUM_INSURED_MIN = 100000.0;
    public static final Double SUM_INSURED_MAX = 300000.0;
    public static final Double PREMIUM_MIN = null;
    public static final Double PREMIUM_MAX = null;
    public static final int MAX_AGE = 59;
    public static final int MIN_AGE = 20;

    @Inject
    private ProductIFineRateRepository productIFineRateRepository;
    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }

        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IFINE_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughInfoToCalculate(productQuotation)) {
            // we need to delete what might have been calculated before
            resetCalculatedStuff(quote, hasIFineCoverage);
            return;
        }

        OccupationType occupationType = occupationTypeRepository.findByOccId(productQuotation.getOccupationId());

        Insured mainInsured = quote.getInsureds().stream().filter(Insured::getMainInsuredIndicator).findFirst().get();

        // copy data already gathered in ProductQuotation
        Integer age = ProductUtils.getAge(productQuotation.getDateOfBirth());
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(age);
        mainInsured.getPerson().setGenderCode(productQuotation.getGenderCode());
        mainInsured.setProfessionId(productQuotation.getOccupationId());
        mainInsured.setProfessionName(occupationType.getOccTextTh());

        // cannot be too young or too old
        ProductUtils.checkInsuredAgeInRange(mainInsured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        mainInsured.setStartDate(now(of(SHORT_IDS.get("VST"))));
        mainInsured.setEndDate(mainInsured.getStartDate().plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(mainInsured.getStartDate().plusYears(DURATION_PAYMENT_IN_YEAR));

        // get iFine package from package name
        ProductIFinePackage productIFinePackage = ProductUtils.validateExistPackageName(ProductIFinePackage.class, productQuotation);
        Double deathByAccident = productIFinePackage.getDeathByAccident();

        // set amounts
        ProductIFinePremium productIFinePremium = quote.getPremiumsData().getProductIFinePremium();
        productIFinePremium.setProductIFinePackage(productIFinePackage);
        productIFinePremium.setSumInsured(amountTHB(productIFinePackage.getSumInsured()));
        productIFinePremium.setAccidentSumInsured(amountTHB(productIFinePackage.getAccidentSumInsured()));
        productIFinePremium.setHealthSumInsured(amountTHB(productIFinePackage.getHealthSumInsured()));
        productIFinePremium.setHospitalizationSumInsured(amountTHB(productIFinePackage.getHospitalizationSumInsured()));
        productIFinePremium.setDeathByAccident(amountTHB(deathByAccident));
        productIFinePremium.setDeathByAccidentInPublicTransport(amountTHB(deathByAccident * 2));
        productIFinePremium.setDisabilityFromAccidentMin(amountTHB(deathByAccident * 2 / 100));
        productIFinePremium.setDisabilityFromAccidentMax(amountTHB(deathByAccident));
        productIFinePremium.setLossOfHandOrLeg(amountTHB(deathByAccident));
        productIFinePremium.setLossOfSight(amountTHB(deathByAccident));
        productIFinePremium.setLossOfHearingMin(amountTHB(deathByAccident * 15 / 100));
        productIFinePremium.setLossOfHearingMax(amountTHB(deathByAccident * 75 / 100));
        productIFinePremium.setLossOfSpeech(amountTHB(deathByAccident * 50 / 100));
        productIFinePremium.setLossOfCorneaForBothEyes(amountTHB(deathByAccident * 50 / 100));
        productIFinePremium.setLossOfFingersMin(amountTHB(deathByAccident * 2 / 100));
        productIFinePremium.setLossOfFingersMax(amountTHB(deathByAccident * 70 / 100));
        productIFinePremium.setNoneCurableBoneFracture(amountTHB(deathByAccident * 10 / 100));
        productIFinePremium.setLegsShortenBy5cm(amountTHB(deathByAccident * 7.5 / 100));
        productIFinePremium.setBurnInjuryMin(amountTHB(deathByAccident * 25 / 100));
        productIFinePremium.setBurnInjuryMax(amountTHB(deathByAccident));
        productIFinePremium.setMedicalCareCost(amountTHB(deathByAccident * 10 / 100));

        // calculates rates
        ProductIFineRate productIFineRate = productIFineRateRepository.findByPlanNameAndGender(productQuotation.getPackageName(), mainInsured.getPerson().getGenderCode().name());
        Double taxDeductibleRate = productIFineRate.getTaxDeductibleRate().get(age - 18);
        Double nonTaxDeductibleRate = productIFineRate.getNonTaxDeductibleRate().get(age - 18);
        Double riskOccupationCharge = 0.0;
        if (occupationType.getOccRisk()) {
            riskOccupationCharge = productIFineRate.getNonTaxDeductibleRiskRate().get(age - 18);
        }
        productIFinePremium.setBasicPremiumRate(taxDeductibleRate);
        productIFinePremium.setRiderPremiumRate(nonTaxDeductibleRate);
        productIFinePremium.setRiskOccupationCharge(riskOccupationCharge);

        // calculate premium
        Double factor = ProductUtils.modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        Double taxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * taxDeductibleRate / 1000 * factor);
        Double nonTaxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * (nonTaxDeductibleRate + riskOccupationCharge) / 1000 * factor);
        Double modalAmount = get2DigitsDouble(productIFinePackage.getSumInsured() * (taxDeductibleRate + nonTaxDeductibleRate + riskOccupationCharge) / 1000 * factor);

        productIFinePremium.setTaxDeductible(amountTHB(taxDeductible));
        productIFinePremium.setNonTaxDeductible(amountTHB(nonTaxDeductible));
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amountTHB(modalAmount));

        if (!hasIFineCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_IFINE_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void createPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(initCommonData());
        ProductUtils.validateMainInsured(quote);

        // There is only one insured at this point
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        ProductUtils.checkInsuredAgeInRange(insured, MIN_AGE, MAX_AGE);
        ProductUtils.checkMainInsured(insured);

        // Recalculate the quote
        //TODO this method do nothing!!!
        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.validateNumberOfCoverages(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkIFinePremiumsData(quote.getPremiumsData());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add payment schedule
        ProductUtils.addPayments(policy, DURATION_PAYMENT_IN_YEAR);
    }

    @Override
    public CommonData initCommonData() {
        CommonData commonData = new CommonData();
        commonData.setProductId(ProductType.PRODUCT_IFINE.getName());
        commonData.setProductCurrency(CURRENCY);
        commonData.setProductName(PRODUCT_IFINE_NAME);
        commonData.setMaxAge(MAX_AGE);
        commonData.setMinAge(MIN_AGE);
        commonData.setMaxPremium(amountTHB(PREMIUM_MAX));
        commonData.setMinPremium(amountTHB(PREMIUM_MIN));
        commonData.setMaxSumInsured(amountTHB(SUM_INSURED_MAX));
        commonData.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        return commonData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData());
        productAmounts.setMaxPremium(amountTHB(PREMIUM_MAX));
        productAmounts.setMaxSumInsured(amountTHB(SUM_INSURED_MAX));
        productAmounts.setMinPremium(amountTHB(PREMIUM_MIN));
        productAmounts.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        return productAmounts;
    }

    @Override
    public PremiumsData initPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIFinePremium productIFinePremium = new ProductIFinePremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIFinePremium(productIFinePremium);

        return premiumsData;
    }

    private boolean hasEnoughInfoToCalculate(ProductQuotation productQuotation) {
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
        boolean hasOccupation = productQuotation.getOccupationId() != null;
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
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductIFinePremium(), PolicyValidationException.premiumnsDataNone);
        notNull(premiumsData.getProductIFinePremium().getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
        notNull(premiumsData.getProductIFinePremium().getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
        notNull(premiumsData.getProductIFinePremium().getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), ProductType.PRODUCT_IFINE.getName(), PolicyValidationException.productIFineExpected);
        isEqual(commonData.getProductName(), PRODUCT_IFINE_NAME, PolicyValidationException.productIFineExpected);
    }

    private Double get2DigitsDouble(Double value) {
        return (double) Math.round(value * 100) / 100;
    }
}
