package th.co.krungthaiaxa.api.elife.products.igen;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ListUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.AmountLimits;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.DateTimeAmount;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductPremiumRateService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.utils.AmountUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

@Service
public class IGenService implements ProductService {

    public static final ProductType PRODUCT_TYPE = ProductType.PRODUCT_IGEN;
    public static final Integer INSURED_COVERAGE_AGE_MAX = null;//No max age for coverage.
    public static final Integer INSURED_COVERAGE_IN_YEAR = 10;
    /**
     * This is the number of years of premiums == duration the customer have to pay premiums.
     */
    public static final Integer INSURED_PAYMENT_IN_YEAR = 6;
    public static final Double TAX_DEDUCTION_PER_YEAR_MAX = 100000.0;//Not sure yet

    public static final String PRODUCT_CURRENCY = ProductUtils.CURRENCY_THB;
    public static final Amount SUM_INSURED_MIN = amount(100000.0);//Calculated from PREMIMUM_PER_MONTH_MIN
    public static final Amount SUM_INSURED_MAX = amount(1500000.0);//1.5M
    public static final Amount PREMIUM_MIN_PER_MONTH = null;//Calculated from SUM_INSURED_MIN
    public static final Amount PREMIUM_MAX_PER_MONTH = null;//Calculated from SUM_INSURED_MAX

    public static final int INSURED_MIN_AGE = 20;
    public static final int INSURED_MAX_AGE = 70;

    public static final Double DIVIDEND_RATE_IN_NORMAL_YEAR = 0.02;//2% this number can be different for other product, and also can be change?
    public static final Double DIVIDEND_RATE_IN_LAST_YEAR = 1.8;
    /**
     * Dividend interest rate when apply 'cash back at the end of contract' option.
     */
    public static final Double DIVIDEND_INTEREST_RATE_FOR_END_OF_CONTRACT = 0.02;//2%
    /**
     * Dividend interest rate when apply 'annual cash back' option.
     */
    public static final Double DIVIDEND_INTEREST_RATE_FOR_ANNUAL = 0.0;//0%

    //All calculation of this product doesn't related to Occupation

    private static final boolean REQUIRED_OCCUPATION_FOR_STORING = true;
    private static final boolean REQUIRED_OCCUPATION_FOR_CALCULATION = false;
    private static final boolean REQUIRED_PACKAGE_NAME_FOR_CALCULATION = false;
    private static final boolean REQUIRED_GENDER_FOR_CALCULATION = false;
    private static final boolean REQUIRED_GENDER_FOR_STORING = true;

    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Inject
    private ProductPremiumRateService productPremiumRateService;

    @Inject
    private BeanValidator beanValidator;

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        Instant now = Instant.now();
        if (productQuotation == null) {
            return;
        }
        Optional<Coverage> hasCoverage = quote.getCoverages()
                .stream()
                .filter(coverage -> coverage.getName() != null)
                .filter(coverage -> coverage.getName().equalsIgnoreCase(PRODUCT_TYPE.getLogicName()))
                .findFirst();

        if (!checkProductQuotationEnoughDataToCalculate(productQuotation)) {
            resetCalculatedStuff(quote, hasCoverage);
            return;
        }
        //Get data from productQuotation
        String packageName = productQuotation.getPackageName();
//        IProtectPackage iProtectPackage = ProductUtils.validateExistPackageName(IProtectPackage.class, productQuotation);

        PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();

        //Get data from quote
        CommonData commonData = quote.getCommonData();
//        commonData.setPackageName(iProtectPackage.name());
        PremiumsData premiumsData = quote.getPremiumsData();
        ProductIGenPremium productIGenPremium = premiumsData.getProductIGenPremium();
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);

        // Copy data from ProductQuotation to Quote
        premiumsData.getFinancialScheduler().getPeriodicity().setCode(periodicityCode);

        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(mainInsuredAge);
        mainInsured.getPerson().setGenderCode(mainInsuredGenderCode);

        double occupationRate = setOccupation(quote, productQuotation, mainInsured);

        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        ProductUtils.checkInsuredAgeInRange(mainInsured, INSURED_MIN_AGE, INSURED_MAX_AGE);

        int paymentYears = INSURED_PAYMENT_IN_YEAR;//iProtectPackage.getPaymentYears();
        int coverageYears = INSURED_COVERAGE_IN_YEAR;//INSURED_COVERAGE_AGE_MAX - mainInsuredAge;
        commonData.setNbOfYearsOfPremium(paymentYears);
        commonData.setNbOfYearsOfCoverage(coverageYears);

        LocalDate startDate = DateTimeUtil.nowLocalDateInThaiZoneId();
        mainInsured.setStartDate(startDate);
        mainInsured.setEndDate(startDate.plusYears(coverageYears));
        premiumsData.getFinancialScheduler().setEndDate(startDate.plusYears(paymentYears));

        //Calculate Premiums input amount (use either PremiumAmount or SumInsuredAmount)
        double premiumRate = validateExistPremiumRate(packageName, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();
        calculateSumInsuredAndPremiumAmounts(premiumsData, productQuotation, packageName, premiumRate, occupationRate, periodicityCode);

        //TODO copy to iProtect
        calculateTax(quote, productIGenPremium, periodicityCode, mainInsured);

        calculateDeathBenefits(now, productIGenPremium, paymentYears, coverageYears, getPremium(quote), periodicityCode);
//        productIGenPremium.setDeathBenefit(productIGenPremium.getSumInsured());

        AmountLimits amountLimits = calculateAmountLimits(packageName, premiumRate, occupationRate, periodicityCode);
        amountLimits.copyToCommonData(commonData);
        validateLimitsForInputAmounts(quote, amountLimits);

        //TODO copy to iProtect
        calculateYearlyCashback(now, quote, productQuotation);

        //In this product, we don't need to calculate yearlyPremium.
        //calculateYearlyPremium(quote, mainInsured);

        if (!hasCoverage.isPresent()) {
            Coverage coverage = new Coverage();
            coverage.setName(PRODUCT_TYPE.getLogicName());
            quote.addCoverage(coverage);
        }
    }

    private OccupationType validateExistOccupationId(Integer occupationId) {
        OccupationType occupationType = occupationTypeRepository.findByOccId(occupationId);
        notNull(occupationType, QuoteCalculationException.occupationNotExistException.apply(occupationId));
        return occupationType;
    }

    private double setOccupation(Quote quote, ProductQuotation productQuotation, Insured mainInsured) {
        double occupationRate;
        if (REQUIRED_OCCUPATION_FOR_STORING || REQUIRED_OCCUPATION_FOR_CALCULATION) {
            OccupationType occupationType = validateExistOccupationId(productQuotation.getOccupationId());
            mainInsured.setProfessionId(occupationType.getOccId());
            mainInsured.setProfessionName(occupationType.getOccTextTh());
            getOccupationRate(occupationType);
            occupationRate = getOccupationRate(occupationType);
        } else {
            occupationRate = 0.0;
        }
        return occupationRate;
    }

    private double validateExistOccupationRateIfNecessary(ProductQuotation productQuotation) {
        if (REQUIRED_OCCUPATION_FOR_STORING || REQUIRED_OCCUPATION_FOR_CALCULATION) {
            OccupationType occupationType = validateExistOccupationId(productQuotation.getOccupationId());
            return getOccupationRate(occupationType);
        } else {
            return 0.0;
        }
    }

    /**
     * @param premium
     * @param periodicityCode periodicity (MONTH, YEAR...) of premium payment.
     */
    private void calculateDeathBenefits(Instant now, ProductIGenPremium productIGenPremium, int paymentYears, int coverageYears, Amount premium, PeriodicityCode periodicityCode) {
        Amount sumInsured = productIGenPremium.getSumInsured();
        Amount premiumInYear = ProductUtils.getPaymentInAYear(premium, periodicityCode);
        List<DateTimeAmount> yearlyDeathBenefits = new ArrayList<>();
        for (int i = 0; i < coverageYears; i++) {
            int year = i + 1;
            DateTimeAmount yearlyDeathBenefit = new DateTimeAmount();
            Amount accumulatedPremiumAmount = premiumInYear.multiply(Math.min(year, paymentYears));
            Amount deathBenefit = AmountUtil.max(sumInsured, accumulatedPremiumAmount);
            yearlyDeathBenefit.setAmount(deathBenefit);
            yearlyDeathBenefit.setDateTime(DateTimeUtil.plusYears(now, year));
            yearlyDeathBenefits.add(yearlyDeathBenefit);
        }
        productIGenPremium.setYearlyDeathBenefits(yearlyDeathBenefits);
    }

    private void calculateTax(Quote quote, ProductIGenPremium productIGenPremium, PeriodicityCode periodicityCode, Insured mainInsured) {
        double taxDeductionPerYear = ProductUtils.calculateTaxDeductionPerYear(TAX_DEDUCTION_PER_YEAR_MAX, getPremium(quote), periodicityCode, mainInsured.getDeclaredTaxPercentAtSubscription());
        productIGenPremium.setYearlyTaxDeduction(amount(taxDeductionPerYear));
        double totalTaxDeduction = taxDeductionPerYear * quote.getCommonData().getNbOfYearsOfPremium();
        productIGenPremium.setTotalTaxDeduction(amount(totalTaxDeduction));
    }
    //TODO UnitTest for many options.

    /**
     * You must set coverage years before calling this method.
     *
     * @param quote
     * @param productQuotation
     */
    private void calculateYearlyCashback(Instant now, Quote quote, ProductQuotation productQuotation) {
        ProductIGenPremium productIGenPremium = quote.getPremiumsData().getProductIGenPremium();
        String dividendOptionId = productQuotation.getDividendOptionId();
        productIGenPremium.setDividendOptionId(dividendOptionId);
        int coverageYears = quote.getCommonData().getNbOfYearsOfCoverage();
        Amount sumInsuredValue = productIGenPremium.getSumInsured();

        List<DateTimeAmount> yearlyCashBackForEndOfContract = calculateYearlyCashBack(sumInsuredValue, coverageYears, now, DIVIDEND_INTEREST_RATE_FOR_END_OF_CONTRACT);
        List<DateTimeAmount> yearlyCashBackForAnnual = calculateYearlyCashBack(sumInsuredValue, coverageYears, now, DIVIDEND_INTEREST_RATE_FOR_ANNUAL);
        productIGenPremium.setYearlyCashBacksForEndOfContract(yearlyCashBackForEndOfContract);
        productIGenPremium.setYearlyCashBacksForAnnual(yearlyCashBackForAnnual);

        Amount endOfContractBenefit;
        if (ProductDividendOption.END_OF_CONTRACT_PAY_BACK.getId().equals(dividendOptionId)) {
            endOfContractBenefit = ListUtil.getLastItem(yearlyCashBackForEndOfContract).getAmount();
        } else {
            endOfContractBenefit = ListUtil.getLastItem(yearlyCashBackForAnnual).getAmount();
        }
        productIGenPremium.setEndOfContractBenefit(endOfContractBenefit);
    }

    private List<DateTimeAmount> calculateYearlyCashBack(Amount sumInsuredValue, int coverageYears, Instant now, double dividendInterestRate) {
        Amount plainAnnualCashBackInNormalYear = sumInsuredValue.multiply(DIVIDEND_RATE_IN_NORMAL_YEAR);
        Amount plainAnnualCashBackInLastYear = sumInsuredValue.multiply(DIVIDEND_RATE_IN_LAST_YEAR);

        List<DateTimeAmount> yearlyCashBacks = new ArrayList<>();
        Amount amountPreviousYear = new Amount(0.0, PRODUCT_CURRENCY);
        for (int i = 0; i < coverageYears; i++) {
            int year = i + 1;
            Amount rootAmount;
            if (i < coverageYears - 1) {//Normal year
                rootAmount = plainAnnualCashBackInNormalYear;
            } else {
                rootAmount = plainAnnualCashBackInLastYear;
            }
            Amount amount = rootAmount.plus(amountPreviousYear.multiply(1 + dividendInterestRate).getValue());
            DateTimeAmount dateTimeAmount = new DateTimeAmount();
            dateTimeAmount.setDateTime(DateTimeUtil.plusYears(now, year));
            dateTimeAmount.setAmount(amount);
            yearlyCashBacks.add(dateTimeAmount);
            amountPreviousYear = dateTimeAmount.getAmount();
        }
        return yearlyCashBacks;
    }

    public static Amount getPremium(Quote quote) {
        return quote.getPremiumsData().getFinancialScheduler().getModalAmount();
    }

    private void calculateSumInsuredAndPremiumAmounts(PremiumsData premiumsData, ProductQuotation productQuotation, String packageName, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        calculateBeforeDiscountForSumInsuredAndPremiumAmounts(premiumsData, productQuotation, packageName, premiumRate, occupationRate, periodicityCode);
        calculateDiscountForSumInsuredAndPremiumAmounts(premiumsData, packageName, premiumRate, occupationRate, periodicityCode);
    }

    private void calculateBeforeDiscountForSumInsuredAndPremiumAmounts(PremiumsData premiumsData, ProductQuotation productQuotation, String packageName, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        ProductIGenPremium ProductIGenPremium = premiumsData.getProductIGenPremium();

        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            Amount sumInsured = exchangeToProductCurrency(productQuotation.getSumInsuredAmount());
            ProductIGenPremium.setSumInsuredBeforeDiscount(sumInsured);
            ProductIGenPremium.setSumInsured(sumInsured);
            ProductIGenPremium.setSumInsuredOption(TRUE);

            Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(ProductIGenPremium.getSumInsured(), premiumRate, periodicityCode);
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
        } else {
            Amount premiumAmount = exchangeToProductCurrency(productQuotation.getPremiumAmount());
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
            premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            ProductIGenPremium.setSumInsuredOption(FALSE);

            Amount sumInsured = ProductUtils.getSumInsuredFromPremium(premiumsData.getFinancialScheduler().getModalAmount(), premiumRate, periodicityCode);
            ProductIGenPremium.setSumInsuredBeforeDiscount(sumInsured);
        }
    }

    private void calculateDiscountForSumInsuredAndPremiumAmounts(PremiumsData premiumsData, String packageName, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        ProductIGenPremium ProductIGenPremium = premiumsData.getProductIGenPremium();

        Amount sumInsuredBeforeDiscount = ProductIGenPremium.getSumInsuredBeforeDiscount();
        double discountRate = getDiscountRate(packageName, sumInsuredBeforeDiscount);
        if (discountRate > 0) {
            if (ProductIGenPremium.getSumInsuredOption()) {
                Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(sumInsuredBeforeDiscount, premiumRate, occupationRate, discountRate, periodicityCode);
                premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            } else {
                Amount premiumAmount = premiumsData.getFinancialScheduler().getModalAmount();
                Amount sumInsuredAmountAfterDiscount = ProductUtils.getSumInsuredFromPremium(premiumAmount, premiumRate, occupationRate, discountRate, periodicityCode);
                ProductIGenPremium.setSumInsured(sumInsuredAmountAfterDiscount);
            }
        } else {
            ProductIGenPremium.setSumInsured(ProductIGenPremium.getSumInsuredBeforeDiscount());
            premiumsData.getFinancialScheduler().setModalAmount(premiumsData.getFinancialScheduler().getModalAmountBeforeDiscount());
        }
    }

    private AmountLimits calculateAmountLimits(String packageName, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        double occupationRateForMaxium = 0.0;
        double discountRateForSumInsuredMax = getDiscountRate(packageName, SUM_INSURED_MAX);
        double discountRateForSumInsuredMin = getDiscountRate(packageName, SUM_INSURED_MIN);

        Amount maxPremiumByPeriodicityAmount = ProductUtils.getPremiumFromSumInsured(SUM_INSURED_MAX, premiumRate, occupationRateForMaxium, discountRateForSumInsuredMax, periodicityCode);
        Amount minPremiumByPeriodicityAmount = ProductUtils.getPremiumFromSumInsured(SUM_INSURED_MIN, premiumRate, occupationRateForMaxium, discountRateForSumInsuredMin, periodicityCode);
        //From premium: calculate the sumInsuredMinium
//        double minPremiumByPeriodicity = ProductUtils.convertPeriodicity(PREMIUM_MIN_PER_MONTH.getValue(), PeriodicityCode.EVERY_MONTH, periodicityCode);
//        Amount minSumInsured = ProductUtils.getSumInsuredFromPremium(PREMIUM_MIN_PER_MONTH, premiumRate, occupationRateForMaxium, discountRateMin, PeriodicityCode.EVERY_MONTH);
        Amount minSumInsured = SUM_INSURED_MIN;
        Amount maxSumInsured = SUM_INSURED_MAX;

        AmountLimits amountLimits = new AmountLimits();
        amountLimits.setMinPremium(minPremiumByPeriodicityAmount);
//        amountLimits.setMinPremium(amount(minPremiumByPeriodicity));
        amountLimits.setMaxPremium(maxPremiumByPeriodicityAmount);
        amountLimits.setMinSumInsured(minSumInsured);
        amountLimits.setMaxSumInsured(maxSumInsured);
        return amountLimits;
    }

    /**
     * Input amount can be either sumInsured or premium
     */
    private void validateLimitsForInputAmounts(Quote quote, AmountLimits amountLimits) {
        Amount sumInsuredAmount = quote.getPremiumsData().getProductIGenPremium().getSumInsured();
        Amount premiumAmount = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        ProductUtils.validateSumInsuredAmountInRange(sumInsuredAmount, amountLimits.getMinSumInsured().getValue(), amountLimits.getMaxSumInsured().getValue());
        ProductUtils.validatePremiumAmountInRange(premiumAmount, amountLimits.getMinPremium().getValue(), amountLimits.getMaxPremium().getValue());
    }

    //TODO I don't remove this method because this code can be used in the future. OK, if you hate it, just remove if you want =)
    private void calculateYearlyPremium(Quote quote, Insured mainInsured) {
//        ProductIGenPremium ProductIGenPremium = quote.getPremiumsData().getProductIGenPremium();
//        List<IProtectMomentCalculation> yearlyCalculations = ProductIGenPremium.getYearlyCalculations();
//        int totalYearsOfCoverage = quote.getCommonData().getNbOfYearsOfCoverage();
//        int totalYearsOfPremiums = quote.getCommonData().getNbOfYearsOfPremium();
//        LocalDate startDate = mainInsured.getStartDate();
//
//        Amount sumInsured = quote.getPremiumsData().getProductIGenPremium().getSumInsured();
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

        ProductIGenPremium ProductIGenPremium = premiumsData.getProductIGenPremium();
        beanValidator.validate(ProductIGenPremium, PolicyValidationException.class);
//        notNull(ProductIGenPremium, PolicyValidationException.premiumnsDataNone);
//        notNull(ProductIGenPremium.getSumInsured(), PolicyValidationException.premiumnsDataNoSumInsured);
//        notNull(ProductIGenPremium.getSumInsured().getCurrencyCode(), PolicyValidationException.premiumnsSumInsuredNoCurrency);
//        notNull(ProductIGenPremium.getSumInsured().getValue(), PolicyValidationException.premiumnsSumInsuredNoAmount);
//        notNull(ProductIGenPremium.getDeathBenefit().getValue(), PolicyValidationException.premiumnsDeathBenefitNoValue);

    }

    @Override
    public CommonData initCommonData() {
        CommonData commonData = new CommonData();
        commonData.setProductId(PRODUCT_TYPE.getLogicName());
        commonData.setProductCurrency(PRODUCT_CURRENCY);

        commonData.setMaxAge(INSURED_MAX_AGE);
        commonData.setMinAge(INSURED_MIN_AGE);

        commonData.setMinSumInsured(SUM_INSURED_MIN);
        commonData.setMaxSumInsured(SUM_INSURED_MAX);
        commonData.setMinPremium(PREMIUM_MIN_PER_MONTH);
        commonData.setMaxPremium(PREMIUM_MAX_PER_MONTH);

        commonData.setNbOfYearsOfCoverage(INSURED_COVERAGE_IN_YEAR);
        commonData.setNbOfYearsOfPremium(INSURED_PAYMENT_IN_YEAR);
        return commonData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
//        IProtectPackage iProtectPackage = ProductUtils.validateExistPackageName(IProtectPackage.class, productQuotation);
        String packageName = productQuotation.getPackageName();
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData(packageName));
        ProductUtils.copyCommonDataToProductAmount(productAmounts.getCommonData(), productAmounts);
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        // get rates from mongoDB
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();
        double premiumRate = validateExistPremiumRate(packageName, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();

        double occupationRate = validateExistOccupationRateIfNecessary(productQuotation);
        AmountLimits amountLimits = calculateAmountLimits(packageName, premiumRate, occupationRate, productQuotation.getPeriodicityCode());
        amountLimits.copyToProductAmounts(productAmounts);
        amountLimits.copyToCommonData(productAmounts.getCommonData());
        return productAmounts;
    }

    private CommonData initCommonData(String packageName) {
        CommonData commonData = initCommonData();
        commonData.setPackageName(packageName);
        commonData.setNbOfYearsOfPremium(INSURED_PAYMENT_IN_YEAR);
        return commonData;
    }

    @Override
    public PremiumsData initPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        ProductIGenPremium productSpecificPremium = new ProductIGenPremium();

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        premiumsData.setProductIGenPremium(productSpecificPremium);

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
        if (REQUIRED_GENDER_FOR_STORING || REQUIRED_GENDER_FOR_CALCULATION) {
            if (productQuotation.getGenderCode() == null) {
                return false;
            }
        }
        if (REQUIRED_PACKAGE_NAME_FOR_CALCULATION) {
            if (StringUtils.isBlank(productQuotation.getPackageName())) {
                return false;
            }
        }
        if (REQUIRED_OCCUPATION_FOR_STORING || REQUIRED_OCCUPATION_FOR_CALCULATION) {
            if (productQuotation.getOccupationId() == null) {
                return false;
            }
        }
        if (AmountUtil.isBlank(productQuotation.getSumInsuredAmount()) && AmountUtil.isBlank(productQuotation.getPremiumAmount())) {
            return false;
        }
        if (productQuotation.getPeriodicityCode() == null) {
            return false;
        }
        if (productQuotation.getDeclaredTaxPercentAtSubscription() == null) {
            return false;
        }
        return true;
    }

    //TODO different for each products, some need packageType, some doesn't need.
    private ProductPremiumRate validateExistPremiumRate(String productPackageLogicName, int insuredAge, GenderCode insuredGenderCode) {
        String productId = PRODUCT_TYPE.getLogicName();
        Optional<ProductPremiumRate> iProtectRateOptional = productPremiumRateService.findPremiumRateByProductId(productId);
        return iProtectRateOptional.orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("productId: %s, age: %s, gender: %s", productId, insuredAge, insuredGenderCode)));
    }

    private static void resetCalculatedStuff(Quote quote, Optional<Coverage> coverage) {
        ProductIGenPremium productIGenPremium = quote.getPremiumsData().getProductIGenPremium();
        if (productIGenPremium != null) {
            productIGenPremium.setSumInsuredBeforeDiscount(null);
            productIGenPremium.setSumInsured(null);
            productIGenPremium.setYearlyDeathBenefits(Collections.EMPTY_LIST);
            productIGenPremium.setYearlyCashBacksForAnnual(Collections.EMPTY_LIST);
            productIGenPremium.setYearlyCashBacksForEndOfContract(Collections.EMPTY_LIST);
            productIGenPremium.setYearlyTaxDeduction(null);
            productIGenPremium.setTotalTaxDeduction(null);
            productIGenPremium.setEndOfContractBenefit(null);
        }
        if (coverage.isPresent()) {
            quote.getCoverages().remove(coverage.get());
        }
    }

    public static void checkMainInsuredIProtectSpecific(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    private static void checkProductType(CommonData commonData) {
        isEqual(commonData.getProductId(), PRODUCT_TYPE.getLogicName(), PolicyValidationException.productIProtectExpected);

        //Don't need to check equals because the displayName can be changed
//        isTrue(StringUtils.isNotBlank(commonData.getProductName()), PolicyValidationException.productIProtectExpected);
    }

    private Amount exchangeToProductCurrency(Amount amount) {
        return ProductUtils.exchangeCurrency(amount, PRODUCT_CURRENCY);
    }

    private double getDiscountRate(String packageName, Amount sumInsuredAmount) {
        return 0.0;//This product doesn't have discount.
    }

    //For this product, the occupation doesn't affect the calculation, so occupationRate is always 0!
    private double getOccupationRate(OccupationType occupationType) {
        return 0.0;
    }

    private static Amount amount(double amountValue) {
        return ProductUtils.amount(amountValue, PRODUCT_CURRENCY);
    }

}
