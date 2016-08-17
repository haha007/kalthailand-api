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
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
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

    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Inject
    private ProductIGenRateRepository productIGenRateRepository;

    @Inject
    private IProtectRateLoaderService iProtectRateLoaderService;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }
        IProtectPackage iProtectPackage = IProtectPackage.valueOf(productQuotation.getPackageName());

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

        OccupationType occupationType = occupationTypeRepository.findByOccId(productQuotation.getOccupationId());
        Insured mainInsured = ProductUtils.getMainInsured(quote);

        //TODO split it to a separated method.
        // copy data from ProductQuotation to Quote
        Integer age = ProductUtils.getAge(productQuotation.getDateOfBirth());
        quote.getPremiumsData().getFinancialScheduler().getPeriodicity().setCode(productQuotation.getPeriodicityCode());
        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(age);
        mainInsured.getPerson().setGenderCode(productQuotation.getGenderCode());
        mainInsured.setProfessionId(productQuotation.getOccupationId());
        mainInsured.setProfessionName(occupationType.getOccTextTh());
        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());

        //Calculate Premiums input amount (use PremiumAmount or SumInsuredAmount)
        PremiumsData premiumsData = quote.getPremiumsData();
        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();

        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            productIProtectPremium.setSumInsured(exchangeToProductCurrency(productQuotation.getSumInsuredAmount()));
            productIProtectPremium.setSumInsuredOption(TRUE);
        } else {
            premiumsData.getFinancialScheduler().setModalAmount(exchangeToProductCurrency(productQuotation.getPremiumAmount()));
            productIProtectPremium.setSumInsuredOption(FALSE);
        }

        // cannot be too young or too old
        ProductUtils.checkInsuredAge(mainInsured, MIN_AGE, MAX_AGE);

        //------------------------------------------------------------------------------------------------------------------------------
        //Above code is the same for every products. Now below code will be different depend on the product
        //------------------------------------------------------------------------------------------------------------------------------

        // Set dates based on current date and product duration
        int paymentYears = iProtectPackage.getPaymentYears();
        int coverageYears = MAX_COVERAGE_AGE - age;//TODO recheck
        LocalDate startDate = DateTimeUtil.nowInThaiZoneId();
        mainInsured.setStartDate(startDate);
        mainInsured.setEndDate(startDate.plusYears(coverageYears));
        premiumsData.getFinancialScheduler().setEndDate(startDate.plusYears(paymentYears));

        // get rates from mongoDB
        //TODO check not found
        IProtectPredefinedRate iprotectPredefinedRate = iProtectRateLoaderService.getPredefinedIProtectRates(iProtectPackage, mainInsured.getAgeAtSubscription()).get();
        //productIProtectRateRepository.findByGender(insured.getPerson().getGenderCode().name());
        //TODO get rate based on insured.getPerson().getGenderCode().name()
        double premiumRate = iprotectPredefinedRate.getFemaleRate(); //productIGenRate.getRate().get(insured.getAgeAtSubscription() - MIN_AGE);

        // calculates premium / sum insured
        PeriodicityCode periodicityCode = quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        //TODO we can merge with above line for calculating sumInsured & premiumAmount
        if (productIProtectPremium.getSumInsured() != null) {
            Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(productIProtectPremium.getSumInsured(), premiumRate, periodicityCode);
            premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
        } else {
            Amount sumInsured = ProductUtils.getSumInsuredFromPremium(premiumsData.getFinancialScheduler().getModalAmount(), premiumRate, periodicityCode);
            premiumsData.getProductIGenPremium().setSumInsured(sumInsured);
        }

        //calculate premiums
        //TODO add more fields to premiums: taxDeductive,
        Double periodFactor = ProductUtils.modalFactor.apply(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode());
        //TODO TaxDeductive
        //TODO Discount
        //TODO Death

        Double taxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * taxDeductibleRate / 1000 * periodFactor);
        Double nonTaxDeductible = get2DigitsDouble(productIFinePackage.getSumInsured() * (nonTaxDeductibleRate + riskOccupationCharge) / 1000 * periodFactor);
        Double modalAmount = get2DigitsDouble(productIFinePackage.getSumInsured() * (taxDeductibleRate + nonTaxDeductibleRate + riskOccupationCharge) / 1000 * periodFactor);

        productIFinePremium.setTaxDeductible(amountTHB(taxDeductible));
        productIFinePremium.setNonTaxDeductible(amountTHB(nonTaxDeductible));
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(amountTHB(modalAmount));

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
        Insured insured = quote.getInsureds().get(0);

        // check main insured stuff
        ProductUtils.checkInsuredAge(insured, MIN_AGE, MAX_AGE);
        ProductUtils.checkMainInsured(insured);
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

        ProductIGenPremium productIGenPremium = new ProductIGenPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIGenPremium(productIGenPremium);

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

    public static void checkMainInsuredIGenSpecific(Insured insured) {
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
}
