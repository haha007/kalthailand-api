package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import th.co.krungthaiaxa.api.common.model.cache.PermanentMemoryCache;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.AmountLimits;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.model.product.ProductSpec;
import th.co.krungthaiaxa.api.elife.model.product.ProductSpecId;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isEqual;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

/**
 * @author khoi.tran on 9/30/16.
 */
public abstract class AbstractProductService implements ProductService {
    private final PermanentMemoryCache<ProductSpecId, ProductSpec> productSpecCache = new PermanentMemoryCache<>();
    @Autowired
    private BeanValidator beanValidator;

    @Autowired
    private ProductPremiumRateService productPremiumRateService;

    @Autowired
    private OccupationTypeRepository occupationTypeRepository;

    abstract protected ProductSpec getProductSpec(ProductSpecId productSpecId);

    protected ProductSpec getProductSpec(ProductQuotation productQuotation) {
        ProductSpecId productSpecId = new ProductSpecId(productQuotation.getProductType().getLogicName(), productQuotation.getPackageName());
        ProductSpec productSpec = productSpecCache.get(productSpecId);
        if (productSpec == null) {
            return getProductSpec(productSpecId);
        } else {
            return productSpec;
        }
    }

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        ProductSpec productSpec = getProductSpec(productQuotation);
        Instant now = Instant.now();
        if (productQuotation == null) {
            return;
        }

        if (!checkProductQuotationEnoughDataToCalculate(productQuotation)) {
            //TODO actually, I think we should throw exception here. Don't return reset quote.
            resetCalculatedStuff(quote);
            return;
        }

        //Get data from productQuotation
        String packageName = productQuotation.getPackageName();
        PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();

        //Get data from quote
        CommonData commonData = quote.getCommonData();
        commonData.setPackageName(productQuotation.getPackageName());
        PremiumsData premiumsData = quote.getPremiumsData();
        PremiumDetail premiumDetail = getPremiumDetail(premiumsData);
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);

        // Copy data from ProductQuotation to Quote
        premiumsData.getFinancialScheduler().getPeriodicity().setCode(periodicityCode);

        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        mainInsured.setAgeAtSubscription(mainInsuredAge);
        mainInsured.getPerson().setGenderCode(mainInsuredGenderCode);

        double occupationRate = setOccupation(quote, productQuotation, mainInsured);

        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
        ProductUtils.checkInsuredAgeInRange(mainInsured, productSpec.getInsuredAgeMin(), productSpec.getInsuredAgeMax());

        int paymentYears = productSpec.getInsuredPaymentYears();
        int coverageYears;
        if (productSpec.getInsuredCoverageYears() != null) {
            coverageYears = productSpec.getInsuredCoverageYears();
        } else if (productSpec.getInsuredCoverageAgeMax() != null) {
            coverageYears = productSpec.getInsuredCoverageAgeMax() - mainInsuredAge;
        } else {
            throw new QuoteCalculationException("ProductSpec error: It must have either insuredCoverageYears or insuredCoverageAgeMax: " + ObjectMapperUtil.toStringMultiLine(productSpec));
        }
        commonData.setNbOfYearsOfPremium(paymentYears);
        commonData.setNbOfYearsOfCoverage(coverageYears);

        calculateDateRange(quote, mainInsured, coverageYears, paymentYears);

        //Calculate Premiums input amount (use either PremiumAmount or SumInsuredAmount)
        double premiumRate = validateExistPremiumRate(productSpec, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();
        calculateSumInsuredAndPremiumAmounts(productSpec, premiumsData, productQuotation, premiumRate, occupationRate, periodicityCode);

        calculateTax(productSpec, quote, premiumDetail, periodicityCode, mainInsured);

        calculateDeathBenefits(now, premiumDetail, paymentYears, coverageYears, ProductUtils.getPremiumAmount(quote), periodicityCode);

        AmountLimits amountLimits = calculateAmountLimits(productSpec, premiumRate, occupationRate, periodicityCode);
        amountLimits.copyToCommonData(commonData);
        validateLimitsForInputAmounts(quote, amountLimits);

        ProductUtils.addCoverageIfNotExist(quote, productSpec.getProductLogicName());
    }

    abstract protected PremiumDetail getPremiumDetail(PremiumsData premiumsData);

    abstract protected void calculateDeathBenefits(Instant now, PremiumDetail premiumDetail, int paymentYears, int coverageYears, Amount premiumAmount, PeriodicityCode periodicityCode);

    protected double setOccupation(Quote quote, ProductQuotation productQuotation, Insured mainInsured) {
        double occupationRate;
        OccupationType occupationType = validateExistOccupationId(productQuotation.getOccupationId());
        mainInsured.setProfessionId(occupationType.getOccId());
        mainInsured.setProfessionName(occupationType.getOccTextTh());
        getOccupationRate(occupationType);
        occupationRate = getOccupationRate(occupationType);
        return occupationRate;
    }

    abstract protected boolean checkProductQuotationEnoughDataToCalculate(ProductQuotation productQuotation);

    abstract protected void resetCalculatedStuff(Quote quote);

    private void calculateDateRange(Quote quote, Insured mainInsured, int coverageYears, int paymentYears) {
        LocalDate startDate = DateTimeUtil.nowLocalDateInThaiZoneId();
        mainInsured.setStartDate(startDate);
        mainInsured.setEndDate(startDate.plusYears(coverageYears));
        quote.getPremiumsData().getFinancialScheduler().setEndDate(startDate.plusYears(paymentYears));
    }

    //TODO migrate data of iProtect to new data structure. Write the initiate code.
    protected ProductPremiumRate validateExistPremiumRate(ProductSpec productSpec, int insuredAge, GenderCode insuredGenderCode) {
        String productId = productSpec.getProductLogicName();
        Optional<ProductPremiumRate> productPremiumRateOptional;

        if (StringUtils.isNotBlank(productSpec.getPackageName())) {
            if (productSpec.isSamePremiumRateAllAges() && productSpec.isSamePremiumRateAllGender()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndPackageName(productSpec.getProductLogicName(), productSpec.getPackageName());
            } else if (productSpec.isSamePremiumRateAllAges()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndPackageNameAndGender(productSpec.getProductLogicName(), productSpec.getPackageName(), insuredGenderCode);
            } else if (productSpec.isSamePremiumRateAllGender()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndPackageNameAndAge(productSpec.getProductLogicName(), productSpec.getPackageName(), insuredAge);
            } else {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndPackageNameAndGenderAndAge(productSpec.getProductLogicName(), productSpec.getPackageName(), insuredGenderCode, insuredAge);
            }
        } else {
            if (productSpec.isSamePremiumRateAllAges() && productSpec.isSamePremiumRateAllGender()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductId(productSpec.getProductLogicName());
            } else if (productSpec.isSamePremiumRateAllAges()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndGender(productSpec.getProductLogicName(), insuredGenderCode);
            } else if (productSpec.isSamePremiumRateAllGender()) {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndAge(productSpec.getProductLogicName(), insuredAge);
            } else {
                productPremiumRateOptional = productPremiumRateService.findPremiumRateByProductIdAndGenderAndAge(productSpec.getProductLogicName(), insuredGenderCode, insuredAge);
            }
        }
        return productPremiumRateOptional.orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("productId: %s, age: %s, gender: %s", productId, insuredAge, insuredGenderCode)));
    }

    protected void calculateSumInsuredAndPremiumAmounts(ProductSpec productSpec, PremiumsData premiumsData, ProductQuotation productQuotation, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        calculateBeforeDiscountForSumInsuredAndPremiumAmounts(productSpec, premiumsData, productQuotation, premiumRate, occupationRate, periodicityCode);
        calculateDiscountForSumInsuredAndPremiumAmounts(productSpec, premiumsData, premiumRate, occupationRate, periodicityCode);
    }

    protected void calculateBeforeDiscountForSumInsuredAndPremiumAmounts(ProductSpec productSpec, PremiumsData premiumsData, ProductQuotation productQuotation, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        PremiumDetail premiumDetail = getPremiumDetail(premiumsData);

        if (productQuotation.getSumInsuredAmount() != null && productQuotation.getSumInsuredAmount().getValue() != null) {
            Amount sumInsured = exchangeToProductCurrency(productSpec, productQuotation.getSumInsuredAmount());
            premiumDetail.setSumInsuredBeforeDiscount(sumInsured);
            premiumDetail.setSumInsured(sumInsured);
            premiumDetail.setSumInsuredOption(TRUE);

            Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(premiumDetail.getSumInsured(), premiumRate, periodicityCode);
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
        } else {
            Amount premiumAmount = exchangeToProductCurrency(productSpec, productQuotation.getPremiumAmount());
            premiumsData.getFinancialScheduler().setModalAmountBeforeDiscount(premiumAmount);
            premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            premiumDetail.setSumInsuredOption(FALSE);

            Amount sumInsured = ProductUtils.getSumInsuredFromPremium(premiumsData.getFinancialScheduler().getModalAmount(), premiumRate, periodicityCode);
            premiumDetail.setSumInsuredBeforeDiscount(sumInsured);
        }
    }

    protected void calculateDiscountForSumInsuredAndPremiumAmounts(ProductSpec productSpec, PremiumsData premiumsData, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        PremiumDetail premiumDetail = getPremiumDetail(premiumsData);

        Amount sumInsuredBeforeDiscount = premiumDetail.getSumInsuredBeforeDiscount();
        double discountRate = getDiscountRate(productSpec, sumInsuredBeforeDiscount);
        if (discountRate > 0) {
            if (premiumDetail.getSumInsuredOption()) {
                Amount premiumAmount = ProductUtils.getPremiumFromSumInsured(sumInsuredBeforeDiscount, premiumRate, occupationRate, discountRate, periodicityCode);
                premiumsData.getFinancialScheduler().setModalAmount(premiumAmount);
            } else {
                Amount premiumAmount = premiumsData.getFinancialScheduler().getModalAmount();
                Amount sumInsuredAmountAfterDiscount = ProductUtils.getSumInsuredFromPremium(premiumAmount, premiumRate, occupationRate, discountRate, periodicityCode);
                premiumDetail.setSumInsured(sumInsuredAmountAfterDiscount);
            }
        } else {
            premiumDetail.setSumInsured(premiumDetail.getSumInsuredBeforeDiscount());
            premiumsData.getFinancialScheduler().setModalAmount(premiumsData.getFinancialScheduler().getModalAmountBeforeDiscount());
        }
    }

    protected Amount exchangeToProductCurrency(ProductSpec productSpec, Amount amount) {
        return ProductUtils.exchangeCurrency(amount, productSpec.getProductCurrency());
    }

    protected Amount amount(ProductSpec productSpec, double amountValue) {
        return ProductUtils.amount(amountValue, productSpec.getProductCurrency());
    }

    protected void calculateTax(ProductSpec productSpec, Quote quote, PremiumDetail premiumDetail, PeriodicityCode periodicityCode, Insured mainInsured) {
        double taxDeductionPerYear = ProductUtils.calculateTaxDeductionPerYear(productSpec.getTaxDeductionPerYearMax(), ProductUtils.getPremiumAmount(quote), periodicityCode, mainInsured.getDeclaredTaxPercentAtSubscription());
        premiumDetail.setYearlyTaxDeduction(amount(productSpec, taxDeductionPerYear));
        double totalTaxDeduction = taxDeductionPerYear * quote.getCommonData().getNbOfYearsOfPremium();
        premiumDetail.setTotalTaxDeduction(amount(productSpec, totalTaxDeduction));
    }

    abstract protected double getDiscountRate(ProductSpec productSpec, Amount sumInsuredAmount);

    abstract protected double getOccupationRate(OccupationType occupationType);

    //TODO currently it only calculate from sumInsured limits to premium limits. Need to support the opposite convertion.
    protected AmountLimits calculateAmountLimits(ProductSpec productSpec, double premiumRate, double occupationRate, PeriodicityCode periodicityCode) {
        double occupationRateForMaxium = 0.0;
        double discountRateForSumInsuredMax = getDiscountRate(productSpec, productSpec.getSumInsuredMax());
        double discountRateForSumInsuredMin = getDiscountRate(productSpec, productSpec.getSumInsuredMin());

        Amount maxPremiumByPeriodicityAmount = ProductUtils.getPremiumFromSumInsured(productSpec.getSumInsuredMax(), premiumRate, occupationRateForMaxium, discountRateForSumInsuredMax, periodicityCode);
        Amount minPremiumByPeriodicityAmount = ProductUtils.getPremiumFromSumInsured(productSpec.getSumInsuredMin(), premiumRate, occupationRateForMaxium, discountRateForSumInsuredMin, periodicityCode);
        //From premium: calculate the sumInsuredMinium
//        double minPremiumByPeriodicity = ProductUtils.convertPeriodicity(PREMIUM_MIN_PER_MONTH.getValue(), PeriodicityCode.EVERY_MONTH, periodicityCode);
//        Amount minSumInsured = ProductUtils.getSumInsuredFromPremium(PREMIUM_MIN_PER_MONTH, premiumRate, occupationRateForMaxium, discountRateMin, PeriodicityCode.EVERY_MONTH);
        Amount minSumInsured = productSpec.getSumInsuredMin();
        Amount maxSumInsured = productSpec.getSumInsuredMax();

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
    protected void validateLimitsForInputAmounts(Quote quote, AmountLimits amountLimits) {
        Amount sumInsuredAmount = getPremiumDetail(quote.getPremiumsData()).getSumInsured();
        Amount premiumAmount = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        ProductUtils.validateSumInsuredAmountInRange(sumInsuredAmount, amountLimits.getMinSumInsured().getValue(), amountLimits.getMaxSumInsured().getValue());
        ProductUtils.validatePremiumAmountInRange(premiumAmount, amountLimits.getMinPremium().getValue(), amountLimits.getMaxPremium().getValue());
    }

    @Override
    public void createPolicyFromQuote(Policy policy, Quote quote) {
        ProductSpec productSpec = getProductSpec(new ProductSpecId(quote.getCommonData().getProductId(), quote.getCommonData().getPackageName()));
        // check for mandatory data

        validateProductType(productSpec, quote.getCommonData());
        //TODO refactor following code!
        Insured insured = ProductUtils.validateMainInsured(quote);

        // check main insured stuff
        ProductUtils.checkInsuredAgeInRange(insured, productSpec.getInsuredAgeMin(), productSpec.getInsuredAgeMax());
        ProductUtils.checkMainInsured(insured);
        checkMainInsuredHasTaxPercent(insured);

        // Recalculate the quote
        //TODO this method do nothing!!!
//        calculateQuote(quote, null);

        // check for calculated data
        ProductUtils.validateNumberOfCoverages(quote.getCoverages());

        // There is only one coverage at this point
        Coverage coverage = quote.getCoverages().get(0);

        ProductUtils.checkBeneficiaries(insured, coverage.getBeneficiaries());
        validatePremiumsData(quote.getPremiumsData());

        // Copy from quote to Policy
        policy.setQuoteId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
        policy.addCoverage(SerializationUtils.clone(coverage));
        policy.addInsured(SerializationUtils.clone(insured));

        // Add payment schedule
        ProductUtils.addPayments(policy, quote.getCommonData().getNbOfYearsOfPremium());
    }

    protected void validateProductType(ProductSpec productSpec, CommonData commonData) {
        isEqual(commonData.getProductId(), productSpec.getProductLogicName(), PolicyValidationException.productIProtectExpected);
    }

    private void validatePremiumsData(PremiumsData premiumsData) {
        notNull(premiumsData, PolicyValidationException.premiumnsDataNone);
        PremiumDetail premiumDetail = getPremiumDetail(premiumsData);
        beanValidator.validate(premiumDetail, PolicyValidationException.class);
    }

    @Override
    public CommonData initCommonData(ProductQuotation productQuotation) {
        ProductSpec productSpec = getProductSpec(productQuotation);
        return initCommonData(productSpec);
    }

    public CommonData initCommonData(ProductSpec productSpec) {
        CommonData commonData = new CommonData();
        commonData.setProductId(productSpec.getProductLogicName());
        commonData.setProductCurrency(productSpec.getProductCurrency());

        commonData.setMaxAge(productSpec.getInsuredAgeMax());
        commonData.setMinAge(productSpec.getInsuredAgeMin());

        commonData.setMinSumInsured(productSpec.getSumInsuredMin());
        commonData.setMaxSumInsured(productSpec.getSumInsuredMax());
        commonData.setMinPremium(productSpec.getPremiumMin());
        commonData.setMaxPremium(productSpec.getPremiumMax());

        commonData.setNbOfYearsOfCoverage(productSpec.getInsuredCoverageYears());
        commonData.setNbOfYearsOfPremium(productSpec.getInsuredPaymentYears());
        return commonData;
    }

    @Override
    public ProductAmounts calculateProductAmounts(ProductQuotation productQuotation) {
        ProductSpec productSpec = getProductSpec(productQuotation);
        ProductAmounts productAmounts = new ProductAmounts();
        productAmounts.setCommonData(initCommonData(productSpec));
        ProductUtils.copyCommonDataToProductAmount(productAmounts.getCommonData(), productAmounts);
        if (productQuotation.getDateOfBirth() == null || productQuotation.getPeriodicityCode() == null) {
            return productAmounts;
        }

        // get rates from mongoDB
        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();
        double premiumRate = validateExistPremiumRate(productSpec, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();

        double occupationRate = validateExistOccupationRateIfNecessary(productQuotation);
        AmountLimits amountLimits = calculateAmountLimits(productSpec, premiumRate, occupationRate, productQuotation.getPeriodicityCode());
        amountLimits.copyToProductAmounts(productAmounts);
        amountLimits.copyToCommonData(productAmounts.getCommonData());
        return productAmounts;
    }

    @Override
    public PremiumsData initPremiumData() {
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setPeriodicity(new Periodicity());

        PremiumsData premiumsData = new PremiumsData();
        premiumsData.setFinancialScheduler(financialScheduler);
        setConstructPremiumDetail(premiumsData);

        return premiumsData;
    }

    protected void checkMainInsuredHasTaxPercent(Insured insured) {
        notNull(insured.getDeclaredTaxPercentAtSubscription(), PolicyValidationException.mainInsuredWithNoDeclaredTax);
    }

    protected abstract void setConstructPremiumDetail(PremiumsData premiumsData);

    private OccupationType validateExistOccupationId(Integer occupationId) {
        OccupationType occupationType = occupationTypeRepository.findByOccId(occupationId);
        notNull(occupationType, QuoteCalculationException.occupationNotExistException.apply(occupationId));
        return occupationType;
    }

    private double validateExistOccupationRateIfNecessary(ProductQuotation productQuotation) {
        OccupationType occupationType = validateExistOccupationId(productQuotation.getOccupationId());
        return getOccupationRate(occupationType);
    }
}
