package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.data.IProtectDiscountRate;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.PremiumDetail;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.ProductSpec;
import th.co.krungthaiaxa.api.elife.model.ProductSpecId;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.AbstractProductService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.AmountUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

@Service
public class IProtectService extends AbstractProductService implements ProductService {
//    private final ProductSpec PRODUCT_SPEC = getProductSpec(ProductType.PRODUCT_IPROTECT.getLogicName(), IProtectPackage.IPROTECT10.name());

//    public static final ProductType PRODUCT_TYPE = ProductType.PRODUCT_IPROTECT;
//    public static final Integer INSURED_COVERAGE_AGE_MAX = 85;
//    public static final Integer INSURED_COVERAGE_IN_YEAR = null;//Calculated based on age.
//    /**
//     * This is the number of years of premiums == duration the customer have to pay premiums.
//     */
//    public final static Integer INSURED_PAYMENT_IN_YEAR = null;//Depend on the iProtectPackage (5, 10 or 85 years)
//    public static final Double TAX_DEDUCTION_PER_YEAR_MAX = 100000.0;
//
//    public static final String PRODUCT_CURRENCY = ProductUtils.CURRENCY_THB;
//    public static final Amount SUM_INSURED_MIN = amount(200000.0);//200K
//    public static final Amount SUM_INSURED_MAX = amount(1500000.0);//1.5M
//    public static final Amount PREMIUM_MIN_PER_MONTH = null; //Calculated from SUM_INSURED_MIN
//    public static final Amount PREMIUM_MAX_PER_MONTH = null; //Calculated from SUM_INSURED_MAX
//
////    public static final int PRODUCT_SPEC.getInsuredAgeMin() = 20;
////    public static final int PRODUCT_SPEC.getInsuredAgeMax() = 55;

    //All calculation of this product doesn't related to Occupation
    public static final boolean REQUIRED_OCCUPATION_FOR_CALCULATION = false;

    @Inject
    private IProtectRateService iProtectRateService;

    @Inject
    private IProtectDiscountRateService iProtectDiscountRateService;

    @Inject
    private BeanValidator beanValidator;

    @Override
    //TODO Improvement: This can be loaded from DB
    public ProductSpec getProductSpec(ProductSpecId productSpecId) {
        String currency = ProductUtils.CURRENCY_THB;

        String packageName = productSpecId.getProductPackageName();
        String productLogicName = productSpecId.getProductLogicName();
        int insuredPaymentYears;//Default
        if (packageName.equals(IProtectPackage.IPROTECT10.name())) {
            insuredPaymentYears = 10;
        } else {
            throw new QuoteCalculationException("Invalid package Name: " + ObjectMapperUtil.toStringMultiLine(productSpecId));
        }

        ProductSpec productSpec = new ProductSpec();
        productSpec.setDividendInterestRateForAnnual(null);
        productSpec.setDividendInterestRateForEndOfContract(null);//2%
        productSpec.setDividendRateInLastYear(null);//180%
        productSpec.setDividendRateInNormalYear(null);//2%
        productSpec.setInsuredAgeMax(55);
        productSpec.setInsuredAgeMin(20);
        productSpec.setInsuredCoverageAgeMax(85);
        productSpec.setInsuredCoverageYears(null);
        productSpec.setInsuredPaymentYears(insuredPaymentYears);
        productSpec.setPackageName(packageName);
        productSpec.setPremiumMax(null);
        productSpec.setPremiumMin(null);
        productSpec.setPremiumLimitsPeriodicityCode(PeriodicityCode.EVERY_MONTH);
        productSpec.setProductCurrency(currency);
        productSpec.setProductLogicName(productLogicName);
        productSpec.setSamePremiumRateAllAges(false);
        productSpec.setSamePremiumRateAllGender(false);
        productSpec.setSumInsuredMax(Amount.amount(1500000.0, currency));
        productSpec.setSumInsuredMin(Amount.amount(200000.0, currency));
        productSpec.setTaxDeductionPerYearMax(100000.0);
        return productSpec;
    }
//
//    @Override
//    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
//        ProductSpec productSpec = PRODUCT_SPEC;
//        Instant now = Instant.now();
//        if (productQuotation == null) {
//            return;
//        }
//
//        if (!checkProductQuotationEnoughDataToCalculate(productQuotation)) {
//            //TODO actually, I think we should throw exception here. Don't return reset quote.
//            resetCalculatedStuff(quote);
//            return;
//        }
//
//        //Get data from productQuotation
//        String packageName = productQuotation.getPackageName();
//        PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
//        GenderCode mainInsuredGenderCode = productQuotation.getGenderCode();
//
//        //Get data from quote
//        CommonData commonData = quote.getCommonData();
//        commonData.setPackageName(productQuotation.getPackageName());
//        PremiumsData premiumsData = quote.getPremiumsData();
//        ProductIProtectPremium productIProtectPremium = premiumsData.getProductIProtectPremium();
//        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
//
//        // Copy data from ProductQuotation to Quote
//        premiumsData.getFinancialScheduler().getPeriodicity().setCode(periodicityCode);
//
//        mainInsured.getPerson().setBirthDate(productQuotation.getDateOfBirth());
//        Integer mainInsuredAge = ProductUtils.getAge(productQuotation.getDateOfBirth());
//        mainInsured.setAgeAtSubscription(mainInsuredAge);
//        mainInsured.getPerson().setGenderCode(mainInsuredGenderCode);
//
//        double occupationRate = setOccupation(quote, productQuotation, mainInsured);
//
//        mainInsured.setDeclaredTaxPercentAtSubscription(productQuotation.getDeclaredTaxPercentAtSubscription());
//        ProductUtils.checkInsuredAgeInRange(mainInsured, productSpec.getInsuredAgeMin(), productSpec.getInsuredAgeMax());
//
//        int paymentYears = productSpec.getInsuredPaymentYears();
//        int coverageYears = productSpec.getInsuredCoverageAgeMax() - mainInsuredAge;
//        commonData.setNbOfYearsOfPremium(paymentYears);
//        commonData.setNbOfYearsOfCoverage(coverageYears);
//
//        calculateDateRange(quote, mainInsured, coverageYears, paymentYears);
//
//        //Calculate Premiums input amount (use either PremiumAmount or SumInsuredAmount)
//        double premiumRate = validateExistPremiumRate(packageName, mainInsuredAge, mainInsuredGenderCode).getPremiumRate();
//        calculateSumInsuredAndPremiumAmounts(premiumsData, productQuotation, packageName, premiumRate, occupationRate, periodicityCode);
//
//        calculateTax(quote, productIProtectPremium, periodicityCode, mainInsured);
//
//        calculateDeathBenefits(now, productIProtectPremium, paymentYears, coverageYears, ProductUtils.getPremiumAmount(quote), periodicityCode);
//
//        AmountLimits amountLimits = calculateAmountLimits(packageName, premiumRate, occupationRate, periodicityCode);
//        amountLimits.copyToCommonData(commonData);
//        validateLimitsForInputAmounts(quote, amountLimits);
//
//        ProductUtils.addCoverageIfNotExist(quote, productSpec.getProductLogicName());
//    }

    /**
     * Abstract
     *
     * @return
     */
    protected ProductIProtectPremium getPremiumDetail(PremiumsData premiumsData) {
        return premiumsData.getProductIProtectPremium();
    }

    protected void calculateDeathBenefits(Instant now, PremiumDetail premiumDetail, int paymentYears, int coverageYears, Amount premium, PeriodicityCode periodicityCode) {
        premiumDetail.setDeathBenefit(premiumDetail.getSumInsured());
    }

    /**
     * @param productQuotation
     * @return don't throw Exception here because we want to reset calculated values if there's not enough information to calculate.
     */
    @Override
    protected boolean checkProductQuotationEnoughDataToCalculate(ProductQuotation productQuotation) {
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
        if (productQuotation.getDeclaredTaxPercentAtSubscription() == null) {
            return false;
        }
        if (productQuotation.getOccupationId() == null) {
            return false;
        }
        return true;
    }

    //TODO use the same data structure with IGen
    //TODO Need to migrate DB.
//    @Override
//    protected IProtectRate validateExistPremiumRate(ProductSpec productSpec, int insuredAge, GenderCode insuredGenderCode) {
//        Optional<IProtectRate> iProtectRateOptional = iProtectRateService.findIProtectRates(productSpec.getPackageName(), insuredAge, insuredGenderCode);
//        return iProtectRateOptional.
//                orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("packageName: %s, age: %s, gender: %s", productSpec.getPackageName(), insuredAge, insuredGenderCode)));
//    }

    @Override
    protected void resetCalculatedStuff(Quote quote) {
        ProductIProtectPremium productIProtectPremium = getPremiumDetail(quote.getPremiumsData());
        if (productIProtectPremium != null) {
            productIProtectPremium.setSumInsuredBeforeDiscount(null);
            productIProtectPremium.setSumInsured(null);
            productIProtectPremium.setDeathBenefit(null);
            productIProtectPremium.setTotalTaxDeduction(null);
            productIProtectPremium.setYearlyTaxDeduction(null);
        }
        quote.getCoverages().clear();
    }

    @Override
    protected double getDiscountRate(ProductSpec productSpec, Amount sumInsuredAmount) {
        Optional<IProtectDiscountRate> iProtectDiscountRateOptional = iProtectDiscountRateService.findIProtectDiscountRate(productSpec.getPackageName(), sumInsuredAmount.getValue());
        return iProtectDiscountRateOptional
                .map(IProtectDiscountRate::getDiscountRate)
                .orElse(0.0);
    }

    //For this product, the occupation doesn't affect the calculation, so occupationRate is always 0!
    @Override
    protected double getOccupationRate(OccupationType occupationType) {
        return 0.0;
    }

    @Override
    protected void setConstructPremiumDetail(PremiumsData premiumsData) {
        premiumsData.setProductIProtectPremium(new ProductIProtectPremium());
    }

}
