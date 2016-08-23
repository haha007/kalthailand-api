package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
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
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.utils.AmountUtil;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

@Service
public class IProtectService implements ProductService {

    private static final int TAX_DEDUCTION_PER_YEAR_MAX = 100000;
    public static final int INSURED_COVERAGE_AGE_MAX = 85;
//    public final static Integer DURATION_PAYMENT_IN_YEAR = null;//Depend on the iProtectPackage (5, 10 or 85 years)

    public final static String PRODUCT_CURRENCY = ProductUtils.CURRENCY_THB;
    //Sum assured min: 200,000 //Cell 'fill in information'!C40

    //    public static final Double SUM_INSURED_MIN = null;//calculate from PREMIUM_MIN
    //200K 'iProtect - Quote & Result'!C45//When calculate to Premium, it must not less than 1000 THB/month
    //282,798 'iProtect - Product Detail'!C39
    //TODO not used yet!
    //SumInsured is always calculated by year!
//    public static final Amount SUM_INSURED_MIN = ProductUtils.amountTHB(282798.0);
    public static final Amount SUM_INSURED_MAX = amount(1500000.0);//1.5M
    //    public static final Amount PREMIUM_MAX_PER_MONTH = ProductUtils.amountTHB(57435.0);//based on UI, not calculate from SUM_INSURED_MAX
    public static final Amount PREMIUM_PER_MONTH_MIN = amount(1000.0);//Minimum Premium/month, Cell 'fill in information'!C39
    public static final int INSURED_MAX_AGE = 70;
    public static final int INSURED_MIN_AGE = 20;
    public static final boolean NEED_OCCUPATION = false;
    //'fill in information'!D11: the limit of premium years is 55

    private static final ProductType PRODUCT_TYPE = ProductType.PRODUCT_IPROTECT;

    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Inject
    private IProtectRateService iProtectRateService;

    @Inject
    private IProtectDiscountRateService iProtectDiscountRateService;

    @Inject
    private BeanValidator beanValidator;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        if (productQuotation == null) {
            return;
        }
        Optional<Coverage> hasIFineCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_TYPE.getDisplayName()))
                .findFirst();

        if (!checkProductQuotationEnoughDataToCalculate(productQuotation)) {
            resetCalculatedStuff(quote, hasIFineCoverage);
            return;
        }
        //Get data from productQuotation
        IProtectPackage iProtectPackage = ProductUtils.validateExistPackageName(IProtectPackage.class, productQuotation);
        PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();

        //Get data from quote
        CommonData commonData = quote.getCommonData();
        PremiumsData premiumsData = quote.getPremiumsData();
        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);

        // Copy data from ProductQuotation to Quote
        //TODO split it to a separated method.
        premiumsData.getFinancialScheduler().getPeriodicity().setCode(periodicityCode);

        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(mainInsuredAge);
        mainInsured.getPerson().setGenderCode(mainInsuredGenderCode);

        double occupationRate = 0.0;
        if (NEED_OCCUPATION) {//In some product, it's optional
            OccupationType occupationType = validateExistOccupationId(productQuotation.getOccupationId());
            mainInsured.setProfessionId(occupationType.getOccId());
            mainInsured.setProfessionName(occupationType.getOccTextTh());
            occupationRate = getOccupationRate(occupationType);
        }

        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        ProductUtils.checkInsuredAgeInRange(mainInsured, INSURED_MIN_AGE, INSURED_MAX_AGE);

        int paymentYears = iProtectPackage.getPaymentYears();
        int coverageYears = INSURED_COVERAGE_AGE_MAX - mainInsuredAge;
        commonData.setNbOfYearsOfPremium(paymentYears);
        commonData.setNbOfYearsOfCoverage(coverageYears);

        LocalDate startDate = DateTimeUtil.nowLocalDateInThaiZoneId();
        mainInsured.setStartDate(startDate);
        mainInsured.setEndDate(startDate.plusYears(coverageYears));
        premiumsData.getFinancialScheduler().setEndDate(startDate.plusYears(paymentYears));

        //Calculate Premiums input amount (use either PremiumAmount or SumInsuredAmount)
        double premiumRate = validateExistPremiumRate(iProtectPackage, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();
        calculateSumInsuredAndPremiumAmounts(premiumsData, productQuotation, iProtectPackage, premiumRate, occupationRate, periodicityCode);

        //------------------------------------------------------------------------------------------------------------------------------
        //Above code is the same for every products. Now below code will be different depend on the product
        //------------------------------------------------------------------------------------------------------------------------------

        //TODO
        double taxDeductionPerYear = Math.min(mainInsured.getDeclaredTaxPercentAtSubscription() * 100000, TAX_DEDUCTION_PER_YEAR_MAX);//Calculate Sheet (SA) * taxPercent
        productIProtectPremium.setYearlyTaxDeduction(amount(taxDeductionPerYear));
        double totalTaxDeduction = taxDeductionPerYear * quote.getCommonData().getNbOfYearsOfCoverage();
        productIProtectPremium.setTotalTaxDeduction(amount(totalTaxDeduction));
        productIProtectPremium.setDeathBenefit(productIProtectPremium.getSumInsured());

        AmountLimits amountLimits = calculateAmountLimits(iProtectPackage, premiumRate, periodicityCode);
        amountLimits.copyToCommonData(commonData);
        validateLimitsForInputAmounts(quote, amountLimits);

//        calculateYearlyPremium(quote, mainInsured);

        if (!hasIFineCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_TYPE.getDisplayName());
            quote.addCoverage(coverage);
        }
    }

    private void calculateSumInsuredAndPremiumAmounts(PremiumsData premiumsData, ProductQuotation productQuotation, IProtectPackage iProtectPackage, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        calculateBeforeDiscountForSumInsuredAndPremiumAmounts(premiumsData, productQuotation, iProtectPackage, premiumRate, occupationRate, periodicityCode);
        calculateDiscountForSumInsuredAndPremiumAmounts(premiumsData, iProtectPackage, premiumRate, occupationRate, periodicityCode);
    }

    private void calculateBeforeDiscountForSumInsuredAndPremiumAmounts(PremiumsData premiumsData, ProductQuotation productQuotation, IProtectPackage iProtectPackage, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();

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
    }

    private void calculateDiscountForSumInsuredAndPremiumAmounts(PremiumsData premiumsData, IProtectPackage iProtectPackage, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();

        Amount sumInsuredBeforeDiscount = productIProtectPremium.getSumInsuredBeforeDiscount();
        double discountRate = getDiscountRate(iProtectPackage, sumInsuredBeforeDiscount);
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
    }

    private OccupationType validateExistOccupationId(Integer occupationId) {
        OccupationType occupationType = occupationTypeRepository.findByOccId(occupationId);
        notNull(occupationType, QuoteCalculationException.occupationNotExistException.apply(occupationId));
        return occupationType;
    }

    private AmountLimits calculateAmountLimits(IProtectPackage iProtectPackage, double premiumRate, PeriodicityCode periodicityCode) {
        double occupationRateForMaxium = 0.0;
        double discountRateForSumInsuredMax = getDiscountRate(iProtectPackage, SUM_INSURED_MAX);
        double discountRateMin = 0.0;

        Amount maxPremiumByPeriodicityAmount = ProductUtils.getPremiumFromSumInsured(SUM_INSURED_MAX, premiumRate, occupationRateForMaxium, discountRateForSumInsuredMax, periodicityCode);
        double minPremiumByPeriodicity = ProductUtils.convertPeriodicity(PREMIUM_PER_MONTH_MIN.getValue(), PeriodicityCode.EVERY_MONTH, periodicityCode);
        Amount minSumInsured = ProductUtils.getSumInsuredFromPremium(PREMIUM_PER_MONTH_MIN, premiumRate, occupationRateForMaxium, discountRateMin, PeriodicityCode.EVERY_MONTH);
        Amount maxSumInsured = SUM_INSURED_MAX;

        AmountLimits amountLimits = new AmountLimits();
        amountLimits.setMinPremium(amount(minPremiumByPeriodicity));
        amountLimits.setMaxPremium(maxPremiumByPeriodicityAmount);
        amountLimits.setMinSumInsured(minSumInsured);
        amountLimits.setMaxSumInsured(maxSumInsured);
        return amountLimits;
    }

    /**
     * Input amount can be either sumInsured or premium
     */
    private void validateLimitsForInputAmounts(Quote quote, AmountLimits amountLimits) {
        Amount sumInsuredAmount = quote.getPremiumsData().getProductIProtectPremium().getSumInsured();
        Amount premiumAmount = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        ProductUtils.validateSumInsuredAmountInRange(sumInsuredAmount, amountLimits.getMinSumInsured().getValue(), amountLimits.getMaxSumInsured().getValue());
        ProductUtils.validatePremiumAmountInRange(premiumAmount, amountLimits.getMinPremium().getValue(), amountLimits.getMaxPremium().getValue());
    }

    private void calculateYearlyPremium(Quote quote, Insured mainInsured) {
//        ProductIProtectPremium productIProtectPremium = quote.getPremiumsData().getProductIProtectPremium();
//        List<IProtectMomentCalculation> yearlyCalculations = productIProtectPremium.getYearlyCalculations();
//        int totalYearsOfCoverage = quote.getCommonData().getNbOfYearsOfCoverage();
//        int totalYearsOfPremiums = quote.getCommonData().getNbOfYearsOfPremium();
//        LocalDate startDate = mainInsured.getStartDate();
//
//        Amount sumInsured = quote.getPremiumsData().getProductIProtectPremium().getSumInsured();
//        Amount premium = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
//        PeriodicityCode periodicityCode = quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
//        Amount yearlyPremium = premium.multiply(periodicityCode.getNbOfMonths());

//        for (int yearIndex = 1; yearIndex <= totalYearsOfCoverage; yearIndex++) {
//            IProtectMomentCalculation iProtectMomentCalculation = new IProtectMomentCalculation();
//            Instant instant = DateTimeUtil.toInstant(startDate.plusYears(yearIndex));
//            iProtectMomentCalculation.setInstant(instant);
//            iProtectMomentCalculation.setDeathBenefit(sumInsured);
//            int yearsOfPremium = (yearIndex <= totalYearsOfPremiums) ? yearIndex : totalYearsOfPremiums;
//            iProtectMomentCalculation.setTotalPremiums(yearlyPremium.multiply(yearsOfPremium));
//
//            iProtectMomentCalculation.setSurrender();
//            yearlyCalculations.add(iProtectMomentCalculation);
//        }
    }

    @Override
    public void createPolicyFromQuote(Policy policy, Quote quote) {
        // check for mandatory data

        checkProductType(quote.getCommonData());
        //TODO refactor following code!
        Insured insured = ProductUtils.validateMainInsured(quote);

        // check main insured stuff
        ProductUtils.checkInsuredAgeInRange(insured, INSURED_MIN_AGE, INSURED_MAX_AGE);
        ProductUtils.checkMainInsured(insured);
        checkMainInsuredIProtectSpecific(insured);

        // Recalculate the quote
        //TODO this method do nothing!!!
//        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.validateNumberOfCoverages(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        checkIProtectPremiumsData(quote.getPremiumsData());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add payment schedule
        ProductUtils.addPayments(policy, quote.getCommonData().getNbOfYearsOfPremium());
    }

    private void checkIProtectPremiumsData(PremiumsData premiumsData) {
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);

        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();
        beanValidator.validate(productIProtectPremium, PolicyValidationException.class);
//        notNull(productIProtectPremium, PolicyValidationException.premiumnsDataNone);
//        notNull(productIProtectPremium.getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
//        notNull(productIProtectPremium.getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
//        notNull(productIProtectPremium.getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);
//        notNull(productIProtectPremium.getDeathBenefit().getValue(), PolicyValidationException.premiumnsDeathBenefitNoValue);

    }

    @Override
    public CommonData initCommonData() {
        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_TYPE.getName());
        commonData.setProductName(PRODUCT_TYPE.getDisplayName());
        commonData.setProductCurrency(PRODUCT_CURRENCY);

        commonData.setMaxAge(INSURED_MAX_AGE);
        commonData.setMinAge(INSURED_MIN_AGE);

        commonData.setMinPremium(PREMIUM_PER_MONTH_MIN);
        commonData.setMaxSumInsured(SUM_INSURED_MAX);
        //They are calculated when calculate quote
//        commonData.setMaxPremium(amountTHB(PREMIUM_MAX_PER_MONTH));
//        commonData.setMaxSumInsured(ProductUtils.exchangeCurrency(SUM_INSURED_MAX));
//        commonData.setMinPremium(ProductUtils.exchangeCurrency(PREMIUM_PER_MONTH_MIN));
//        commonData.setMinSumInsured(amountTHB(SUM_INSURED_MIN));

//        commonData.setNbOfYearsOfCoverage(DURATION_COVERAGE_IN_YEAR);
//        commonData.setNbOfYearsOfPremium(DURATION_PAYMENT_IN_YEAR);
        return commonData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
        IProtectPackage iProtectPackage = ProductUtils.validateExistPackageName(IProtectPackage.class, productQuotation);
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData(iProtectPackage));
        copyCommonDataToProductAmount(productAmounts.getCommonData(), productAmounts);
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        // get rates from mongoDB
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();
        double premiumRate = validateExistPremiumRate(iProtectPackage, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();
        AmountLimits amountLimits = calculateAmountLimits(iProtectPackage, premiumRate, productQuotation.getPeriodicityCode());
        amountLimits.copyToProductAmounts(productAmounts);
        amountLimits.copyToCommonData(productAmounts.getCommonData());
        return productAmounts;
    }

    private CommonData initCommonData(IProtectPackage iProtectPackage) {
        CommonData commonData = initCommonData();
        commonData.setNbOfYearsOfPremium(iProtectPackage.getPaymentYears());
        return commonData;
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

    /**
     * @param productQuotation
     * @return don't throw Exception here because we want to reset calculated values if there's not enough information to calculate.
     */
    private boolean checkProductQuotationEnoughDataToCalculate(ProductQuotation productQuotation) {
        if (productQuotation.getDateOfBirth() == null) {
            return false;
        }
        if (productQuotation.getGenderCode() == null) {
            return false;
        }
        if (StringUtils.isBlank(productQuotation.getPackageName())) {
            return false;
        }
        if (AmountUtil.isBlank(productQuotation.getSumInsuredAmount()) && AmountUtil.isBlank(productQuotation.getPremiumAmount())) {
            return false;
        }
        if (productQuotation.getPeriodicityCode() == null) {
            return false;
        }

        if (NEED_OCCUPATION) {
            if (productQuotation.getOccupationId() == null) {
                return false;
            }
        }
        return true;
    }

    private IProtectRate validateExistPremiumRate(IProtectPackage iProtectPackage, int insuredAge, GenderCode insuredGenderCode) {
        Optional<IProtectRate> iProtectRateOptional = iProtectRateService.findIProtectRates(iProtectPackage, insuredAge, insuredGenderCode);
        return iProtectRateOptional.
                orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("packageName: %s, age: %s, gender: %s", iProtectPackage, insuredAge, insuredGenderCode)));
    }

    //TODO not completed.
    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        ProductIProtectPremium productIProtectPremium = quote.getPremiumsData().getProductIProtectPremium();
        if (productIProtectPremium != null) {
            productIProtectPremium.setSumInsuredBeforeDiscount(null);
            productIProtectPremium.setSumInsured(null);
            productIProtectPremium.setDeathBenefit(null);
            productIProtectPremium.setTotalTaxDeduction(null);
            productIProtectPremium.setYearlyTaxDeduction(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsuredIProtectSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    //TODO make a common method

    private static void checkProductType(CommonData commonData) {
        isEqual(commonData.getProductId(), PRODUCT_TYPE.getName(), PolicyValidationException.productIProtectExpected);

        //TODO don't need to check it because the displayName can be changed???
        //isEqual(commonData.getProductName(), PRODUCT_TYPE.getDisplayName(), PolicyValidationException.productIProtectExpected);
        isTrue(StringUtils.isNotBlank(commonData.getProductName()), PolicyValidationException.productIProtectExpected);
    }

    private Amount exchangeToProductCurrency(Amount amount) {
        return ProductUtils.exchangeCurrency(amount, PRODUCT_CURRENCY);
    }

    private double getDiscountRate(IProtectPackage iProtectPackage, Amount sumInsuredAmount) {
        Optional<IProtectDiscountRate> iProtectDiscountRateOptional = iProtectDiscountRateService.findIProtectDiscountRate(iProtectPackage, sumInsuredAmount.getValue());
        return iProtectDiscountRateOptional
                .map(IProtectDiscountRate::getDiscountRate)
                .orElse(0.0);
    }

    //For this product, the occupation doesn't affect the calculation, so occupationRate is always 0!
    private double getOccupationRate(OccupationType occupationType) {
        return 0.0;
    }

    private static Amount amount(double amountValue) {
        return ProductUtils.amount(amountValue, PRODUCT_CURRENCY);
    }

    public static class AmountLimits {
        private Amount minPremium;
        private Amount maxPremium;
        private Amount minSumInsured;
        private Amount maxSumInsured;

        public void copyToCommonData(CommonData destination) {
            destination.setMaxSumInsured(maxSumInsured);
            destination.setMinSumInsured(minSumInsured);
            destination.setMinPremium(minPremium);
            destination.setMaxPremium(maxPremium);
        }

        public void copyToProductAmounts(ProductAmounts destination) {
            destination.setMaxSumInsured(maxSumInsured);
            destination.setMinSumInsured(minSumInsured);
            destination.setMinPremium(minPremium);
            destination.setMaxPremium(maxPremium);
        }

        public Amount getMinPremium() {
            return minPremium;
        }

        public void setMinPremium(Amount minPremium) {
            this.minPremium = minPremium;
        }

        public Amount getMaxPremium() {
            return maxPremium;
        }

        public void setMaxPremium(Amount maxPremium) {
            this.maxPremium = maxPremium;
        }

        public Amount getMinSumInsured() {
            return minSumInsured;
        }

        public void setMinSumInsured(Amount minSumInsured) {
            this.minSumInsured = minSumInsured;
        }

        public Amount getMaxSumInsured() {
            return maxSumInsured;
        }

        public void setMaxSumInsured(Amount maxSumInsured) {
            this.maxSumInsured = maxSumInsured;
        }

    }

    private void copyCommonDataToProductAmount(CommonData commonData, ProductAmounts destination) {
        destination.setMaxSumInsured(commonData.getMaxSumInsured());
        destination.setMinSumInsured(commonData.getMinSumInsured());
        destination.setMinPremium(commonData.getMinPremium());
        destination.setMaxPremium(commonData.getMaxPremium());
    }
}
