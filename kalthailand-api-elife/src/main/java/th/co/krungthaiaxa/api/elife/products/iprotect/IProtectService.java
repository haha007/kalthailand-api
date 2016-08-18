package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.DatedAmount;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductIGenRate;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.repository.ProductIGenRateRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isFalse;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amountTHB;

@Service
public class IProtectService implements ProductService {

    private static final int MAX_TAX_DEDUCTION_PER_YEAR = 100000;
    public static final int MAX_COVERAGE_AGE = 85;
    //DURATION_COVERAGE_IN_YEAR = MAX_COVERAGE_AGE - age;
    //public final static int DURATION_COVERAGE_IN_YEAR = 85;//Protect maximum 85 (iProtect85): year protect = 85 - current age.
//    public final static Integer DURATION_PAYMENT_IN_YEAR = null;//Depend on the iProtectPackage (5, 10 or 85 years)
    public final static String PRODUCT_NAME = "Product iProtect";

    public final static String PRODUCT_CURRENCY = ProductUtils.CURRENCY_THB;
    //Sum assured min: 200,000 //Cell 'fill in information'!C40

    //    public static final Double SUM_INSURED_MIN = null;//calculate from PREMIUM_MIN
    //200K 'iProtect - Quote & Result'!C45//When calculate to Premium, it must not less than 1000 THB/month
    //282,798 'iProtect - Product Detail'!C39
    //TODO not used yet!
    public static final Amount SUM_INSURED_MIN = ProductUtils.amountTHB(282798.0);
    public static final Amount SUM_INSURED_MAX = ProductUtils.amountTHB(1500000.0);//1.5M
    //    public static final Amount PREMIUM_MAX = null;//calculate from SUM_INSURED_MAX
    public static final Amount PREMIUM_MIN = ProductUtils.amountTHB(1000.0);//Minimum Premium/month, Cell 'fill in information'!C39
    public static final int MAX_AGE = 70;
    public static final int MIN_AGE = 20;
    //'fill in information'!D11: the limit of premium years is 55

    private static final List<Double> discountRate = Arrays.asList();

    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Inject
    private ProductIGenRateRepository productIGenRateRepository;

    @Inject
    private IProtectRateService iProtectRateService;

    @Inject
    private IProtectDiscountRateService iProtectDiscountRateService;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }
        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_NAME))
                .findFirst();
        // Do we have enough to calculate anything
        if (!validateProductQuotation(productQuotation)) {
            // we need to delete what might have been calculated before
            //TODO In which case we have to reset the quote???
            resetCalculatedStuff(quote, hasIFineCoverage);
            return;
        }
        //Get data from productQuotation
        IProtectPackage iProtectPackage = IProtectPackage.valueOf(productQuotation.getPackageName());
        PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();
        OccupationType occupationType = occupationTypeRepository.findByOccId(productQuotation.getOccupationId());

        //Get data from quote
        PremiumsData premiumsData = quote.getPremiumsData();
        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();

        Insured mainInsured = ProductUtils.getMainInsured(quote);
        //TODO split it to a separated method.
        // copy data from ProductQuotation to Quote
        Integer age = ProductUtils.getAge(productQuotation.getDateOfBirth());
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(periodicityCode);
        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(age);
        mainInsured.getPerson().setGenderCode(mainInsuredGenderCode);
        mainInsured.setProfessionId(productQuotation.getOccupationId());
        mainInsured.setProfessionName(occupationType.getOccTextTh());
        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        ProductUtils.checkInsuredAge(mainInsured, MIN_AGE, MAX_AGE);

        int paymentYears = iProtectPackage.getPaymentYears();
        int coverageYears = MAX_COVERAGE_AGE - age;//TODO recheck
        LocalDate startDate = DateTimeUtil.nowInThaiZoneId();
        mainInsured.setStartDate(startDate);
        mainInsured.setEndDate(startDate.plusYears(coverageYears));
        premiumsData.getFinancialScheduler().setEndDate(startDate.plusYears(paymentYears));

        //Calculate Premiums input amount (use either PremiumAmount or SumInsuredAmount)

        IProtectRate iProtectRate = validateExistPremiumRate(iProtectPackage, mainInsured);
        double premiumRate = iProtectRate.getPremiumRate();
        double occupationRate = getOccupationRate(occupationType);

        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            Amount sumInsured = exchangeToProductCurrency(productQuotation.getSumInsuredAmount());
            productIProtectPremium.setSumInsuredBeforeDiscount(sumInsured);
            productIProtectPremium.setSumInsured(sumInsured);
            productIProtectPremium.setSumInsuredOption(TRUE);

            Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(productIProtectPremium.getSumInsured(), premiumRate, periodicityCode);
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
        } else {
            Amount premiumAmount = exchangeToProductCurrency(productQuotation.getPremiumAmount());
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
            premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            productIProtectPremium.setSumInsuredOption(FALSE);

            Amount sumInsured = ProductUtils.getSumInsuredFromPremium(premiumsData.getFinancialScheduler().getModalAmount(), premiumRate, periodicityCode);
            productIProtectPremium.setSumInsuredBeforeDiscount(sumInsured);
        }

        //Discount
        Amount sumInsuredBeforeDiscount = productIProtectPremium.getSumInsuredBeforeDiscount();
        double discountRate = getDiscountRate(iProtectPackage, productIProtectPremium, sumInsuredBeforeDiscount);
        if (discountRate > 0) {
            if (productIProtectPremium.getSumInsuredOption()) {
                Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(sumInsuredBeforeDiscount, premiumRate, occupationRate, discountRate, periodicityCode);
                premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            } else {
                Amount premiumAmount = premiumsData.getFinancialScheduler().getModalAmount();
                Amount sumInsuredAmountAfterDiscount = ProductUtils.getSumInsuredFromPremium(premiumAmount, premiumRate, occupationRate, discountRate, periodicityCode);
                productIProtectPremium.setSumInsured(sumInsuredAmountAfterDiscount);
            }
        } else {
            productIProtectPremium.setSumInsured(productIProtectPremium.getSumInsuredBeforeDiscount());
            premiumsData.getFinancialScheduler().setModalAmount(premiumsData.getFinancialScheduler().getModalAmountBeforeDiscount());
        }

        //------------------------------------------------------------------------------------------------------------------------------
        //Above code is the same for every products. Now below code will be different depend on the product
        //------------------------------------------------------------------------------------------------------------------------------

        //TODO add more fields to premiums: taxDeductive,
        Double periodFactor = ProductUtils.modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        double taxDeductionPerYear = Math.min(mainInsured.getDeclaredTaxPercentAtSubscription() * 100000, MAX_TAX_DEDUCTION_PER_YEAR);//Calculate Sheet (SA) * taxPercent
        productIProtectPremium.setYearlyTaxDeduction(amount(taxDeductionPerYear));
        double totalTaxDeduction = taxDeductionPerYear * quote.getCommonData().getNbOfYearsOfCoverage();
        productIProtectPremium.setTotalTaxDeduction(amount(totalTaxDeduction));

        //TODO Death: OK
        productIProtectPremium.setDeathBenefit(productIProtectPremium.getSumInsured());

        // cannot insure too much or not enough
        //TODO compare sumInsureMin & premiumMax to current premiumData with the same periodicity.
        Amount premiumMax = ProductUtils.getPremiumFromSumInsured(SUM_INSURED_MAX, premiumRate, periodicityCode);
        Amount sumInsuredMin = ProductUtils.getSumInsuredFromPremium(PREMIUM_MIN, premiumRate, periodicityCode);
        checkSumInsured(productIProtectPremium, PRODUCT_CURRENCY, sumInsuredMin.getValue(), SUM_INSURED_MAX.getValue());
        checkPremium(premiumsData, PREMIUM_MIN.getValue(), premiumMax.getValue());

        if (!hasIFineCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_NAME);
            quote.addCoverage(coverage);
        }
    }

    @Override
    public void getPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data
        checkCommonData(initCommonData());
        ProductUtils.checkInsured(quote);

        // There is only one insured at this point
        Insured insured = ProductUtils.getMainInsured(quote);

        // check main insured stuff
        ProductUtils.checkInsuredAge(insured, MIN_AGE, MAX_AGE);
        ProductUtils.checkMainInsured(insured);
        //TODO must replaced by IProtect
        checkMainInsuredIGenSpecific(insured);

        // Recalculate the quote
        //TODO this method do nothing!!!
//        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.checkCoverage(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkIGenPremiumsData(quote.getPremiumsData(), insured.getStartDate());

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
        commonData.setMaxAge(MAX_AGE);
        commonData.setMaxPremium(amountTHB(PREMIUM_MAX));
        commonData.setMaxSumInsured(ProductUtils.exchangeCurrency(SUM_INSURED_MAX));
        commonData.setMinAge(MIN_AGE);
        commonData.setMinPremium(ProductUtils.exchangeCurrency(PREMIUM_MIN));
        commonData.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        commonData.setProductId(ProductType.PRODUCT_IGEN.getName());
        commonData.setProductCurrency(PRODUCT_CURRENCY);
        commonData.setProductName(PRODUCT_NAME);
        return commonData;
    }

    @Override
    public ProductAmounts initProductAmounts(ProductQuotation productQuotation) {
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData());
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        // get rates from mongoDB
        ProductIGenRate productIGenRate = productIGenRateRepository.findByGender(productQuotation.getGenderCode().name());
        Double interestRate = productIGenRate.getRate().get(ProductUtils.getAge(productQuotation.getDateOfBirth()) - MIN_AGE);
        Double factor = ProductUtils.modalFactor.apply(productQuotation.getPeriodicityCode());
        productAmounts.setMaxPremium(amountTHB(SUM_INSURED_MAX * factor * interestRate / 1000));
        productAmounts.setMaxSumInsured(ProductUtils.exchangeCurrency(SUM_INSURED_MAX));
        productAmounts.setMinPremium(amountTHB(SUM_INSURED_MIN * factor * interestRate / 1000));
        productAmounts.setMinSumInsured(amountTHB(SUM_INSURED_MIN));
        return productAmounts;
    }

    @Override
    public PremiumsData initPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIProtectPremium productSpecificPremium = new ProductIProtectPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIProtectPremium(productSpecificPremium);

        return premiumsData;
    }

    private boolean validateProductQuotation(ProductQuotation productQuotation) {
        if (productQuotation.getDateOfBirth() == null) {
            return false;
        }
        if (productQuotation.getGenderCode() == null) {
            return false;
        }
        if (productQuotation.getOccupationId() == null) {
            return false;
        }
        if (productQuotation.getPackageName() == null) {
            return false;
        }
        return productQuotation.getPeriodicityCode() != null;
    }

    private IProtectRate validateExistPremiumRate(IProtectPackage iProtectPackage, Insured insured) {
        int age = insured.getAgeAtSubscription();
        GenderCode mainInsuredGenderCode = insured.getPerson().getGenderCode();

        //productIGenRate.getRate().get(insured.getAgeAtSubscription() - MIN_AGE);
        Optional<IProtectRate> iProtectRateOptional = iProtectRateService.findIProtectRates(iProtectPackage, age, mainInsuredGenderCode);
        return iProtectRateOptional.
                orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("packageName: %s, age: %s, gender: %s", iProtectPackage, age, mainInsuredGenderCode)));
    }

    //TODO not completed.
    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        ProductIProtectPremium productIProtectPremium = quote.getPremiumsData().getProductIProtectPremium();
        if (productIProtectPremium != null) {
            productIProtectPremium.setYearlyCashBacksAverageBenefit(new ArrayList<>());
            productIProtectPremium.setYearlyCashBacksAverageDividende(new ArrayList<>());
            productIProtectPremium.setEndOfContractBenefitsAverage(new ArrayList<>());
            productIProtectPremium.setYearlyCashBacksMaximumBenefit(new ArrayList<>());
            productIProtectPremium.setYearlyCashBacksMaximumDividende(new ArrayList<>());
            productIProtectPremium.setEndOfContractBenefitsMaximum(new ArrayList<>());
            productIProtectPremium.setEndOfContractBenefitsMinimum(new ArrayList<>());
            productIProtectPremium.setYearlyCashBacks(new ArrayList<>());
            productIProtectPremium.setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsuredIProtectSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    private static void checkSumInsured(ProductIProtectPremium productIProtectPremium, String currency, Double sumInsuredMin, Double sumInsuredMax) {
        if (productIProtectPremium.getSumInsured() == null || productIProtectPremium.getSumInsured().getValue() == null) {
            // no amount to check
            return;
        }
        isEqual(currency, productIProtectPremium.getSumInsured().getCurrencyCode(), QuoteCalculationException.sumInsuredCurrencyException.apply(currency));
        isFalse(productIProtectPremium.getSumInsured().getValue() > sumInsuredMax, QuoteCalculationException.sumInsuredTooHighException.apply(sumInsuredMax));
        isFalse(productIProtectPremium.getSumInsured().getValue() < sumInsuredMin, QuoteCalculationException.sumInsuredTooLowException.apply(sumInsuredMin));
    }

    private static void checkCommonData(CommonData commonData) {
        isEqual(commonData.getProductId(), ProductType.PRODUCT_IGEN.getName(), PolicyValidationException.productIGenExpected);
        isEqual(commonData.getProductName(), PRODUCT_NAME, PolicyValidationException.productIGenExpected);
    }

    //TODO make a common method
    //Should be the same for all of Products
    private static void checkPremium(PremiumsData premiumsData, double premiumMin, double premiumMax) {
        Amount modalAmount = premiumsData.getFinancialScheduler().getModalAmount();
        if (modalAmount == null || modalAmount.getValue() == null) {
            return;
        }

        isEqual(PRODUCT_CURRENCY, modalAmount.getCurrencyCode(), QuoteCalculationException.premiumCurrencyException.apply(PRODUCT_CURRENCY));
        isTrue(modalAmount.getValue() >= premiumMin, QuoteCalculationException.premiumTooLowException.apply(premiumMin));
        isTrue(modalAmount.getValue() <= premiumMax, QuoteCalculationException.premiumTooHighException.apply(premiumMax));
    }

    private static List<DatedAmount> calculateDatedAmount(Quote quote, Integer percentRate, List<Double> dividends) {
        List<DatedAmount> result = new ArrayList<>();
        Amount sumInsured = productIProtectPremium.getSumInsured();
        LocalDate startDate = quote.getInsureds().get(0).getStartDate();
        Double latestAmout = 0.0;
        for (int i = 1; i <= DURATION_COVERAGE_IN_YEAR; i++) {
            Double interest = sumInsured.getValue() * dividends.get(i - 1) / 1000;
            DatedAmount datedAmount = new DatedAmount();
            datedAmount.setCurrencyCode(sumInsured.getCurrencyCode());
            datedAmount.setDate(startDate.plusYears(i));
            if (percentRate != null) {
                latestAmout = (double) Math.round(interest + latestAmout + (latestAmout * percentRate) / 1000);
                datedAmount.setValue(latestAmout);
            } else {
                datedAmount.setValue(interest);
            }
            result.add(datedAmount);
        }
        Collections.sort(result);
        return result;
    }

    private Amount exchangeToProductCurrency(Amount amount) {
        return ProductUtils.exchangeCurrency(amount, PRODUCT_CURRENCY);
    }

    private double getDiscountRate(IProtectPackage iProtectPackage, ProductIProtectPremium productIProtectPremium, Amount sumInsuredAmount) {
        Optional<IProtectDiscountRate> iProtectDiscountRateOptional = iProtectDiscountRateService.findIProtectDiscountRate(iProtectPackage, productIProtectPremium.getSumInsured().getValue());
        return iProtectDiscountRateOptional
                .map(IProtectDiscountRate::getDiscountRate)
                .orElseThrow(() -> QuoteCalculationException.sumInsuredTooLowException.apply(sumInsuredAmount.getValue()));
    }

    //TODO for this product, the occupation doesn't affect the calculation, so occupationRate is always 0!
    private double getOccupationRate(OccupationType occupationType) {
        return 0.0;
    }

    private Amount amount(double amountValue) {
        return ProductUtils.amount(amountValue, PRODUCT_CURRENCY);
    }
}
