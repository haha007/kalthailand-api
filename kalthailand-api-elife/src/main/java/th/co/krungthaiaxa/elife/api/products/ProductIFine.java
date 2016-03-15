package th.co.krungthaiaxa.elife.api.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage;
import th.co.krungthaiaxa.elife.api.repository.ProductIFineRateRepository;

import javax.inject.Inject;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.checkMainInsuredAge;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.getAge;
import static th.co.krungthaiaxa.elife.api.products.ProductUtils.modalFactor;

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
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) throws QuoteCalculationException {
        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_IFINE_NAME))
                .findFirst();

        // Do we have enough to calculate anything
        if (!hasEnoughTocalculate(productQuotation)) {
            // we need to delete what might have been calculated before
//            resetCalculatedStuff(quote, hasIFineCoverage);
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
        checkMainInsuredAge(insured, MIN_AGE, MAX_AGE);

        // Set dates based on current date and product duration
        insured.setStartDate(now(of(SHORT_IDS.get("VST"))));
        insured.setEndDate(insured.getStartDate().plusYears(DURATION_COVERAGE_IN_YEAR));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(insured.getStartDate().plusYears(DURATION_PAYMENT_IN_YEAR));

        // get iFine package from package name
        ProductIFinePackage productIFinePackage = getPackage(productQuotation.getPackageName());

        // set sum insured
        Amount amount = new Amount();
        amount.setCurrencyCode(PRODUCT_IFINE_CURRENCY);
        amount.setValue(productIFinePackage.getSumInsured());
        quote.getPremiumsData().getProductIFinePremium().setSumInsured(amount);

        // calculates rates
        PremiumsData premiumsData = quote.getPremiumsData();
        ProductIFineRate productIFineRate = productIFineRateRepository.findByPlanNameAndGender(productQuotation.getPackageName(), insured.getPerson().getGenderCode().name());
        Double taxDeductibleRate = productIFineRate.getTaxDeductibleRate().get(age - 18);
        Double nonTaxDeductibleRate = productIFineRate.getNonTaxDeductibleRate().get(age - 18);
        Double riskOccupationCharge = 0.0;
        if (productQuotation.getRiskOccupation()) {
            riskOccupationCharge = productIFineRate.getNonTaxDeductibleRiskRate().get(age - 18);
        }
        premiumsData.getProductIFinePremium().setBasicPremiumRate(taxDeductibleRate);
        premiumsData.getProductIFinePremium().setRiderPremiumRate(nonTaxDeductibleRate);
        premiumsData.getProductIFinePremium().setRiskOccupationCharge(riskOccupationCharge);

        // calculate amounts
        Double factor = modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        Double taxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * taxDeductibleRate / 1000 * factor);
        Double nonTaxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * (nonTaxDeductibleRate + riskOccupationCharge) / 1000 * factor);
        premiumsData.getProductIFinePremium().setTaxDeductible(amount(taxDeductible));
        premiumsData.getProductIFinePremium().setNonTaxDeductible(amount(nonTaxDeductible));
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amount(taxDeductible + nonTaxDeductible));
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) throws PolicyValidationException, QuoteCalculationException {

    }

    @Override
    public CommonData getCommonData() {
        return null;
    }

    @Override
    public ProductAmounts getProductAmounts(ProductQuotation productQuotation) {
        return null;
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

    private ProductIFinePackage getPackage(String packageName) throws QuoteCalculationException {
        ProductIFinePackage result = ProductIFinePackage.valueOf(packageName);
        if (result == null) {
            throw QuoteCalculationException.iFinePackageNameUnknown;
        }
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
