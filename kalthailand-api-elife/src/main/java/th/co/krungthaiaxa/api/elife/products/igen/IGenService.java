package th.co.krungthaiaxa.api.elife.products.igen;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ListUtil;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.DateTimeAmount;
import th.co.krungthaiaxa.api.elife.model.PremiumDetail;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.ProductSpec;
import th.co.krungthaiaxa.api.elife.model.ProductSpecId;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.AbstractProductService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.OccupationTypeRepository;
import th.co.krungthaiaxa.api.elife.utils.AmountUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IGenService extends AbstractProductService implements ProductService {
    @Inject
    private OccupationTypeRepository occupationTypeRepository;

    @Override
    protected ProductSpec getProductSpec(ProductSpecId productSpecId) {
        String currency = ProductUtils.CURRENCY_THB;
        ProductSpec productSpec = new ProductSpec();
        productSpec.setDividendInterestRateForAnnual(0.0);
        productSpec.setDividendInterestRateForEndOfContract(0.02);//2%
        productSpec.setDividendRateInLastYear(1.8);//180%
        productSpec.setDividendRateInNormalYear(0.02);//2%
        productSpec.setInsuredAgeMax(70);
        productSpec.setInsuredAgeMin(20);
        productSpec.setInsuredCoverageAgeMax(null);
        productSpec.setInsuredCoverageYears(10);
        productSpec.setInsuredPaymentYears(6);
        productSpec.setPackageName(productSpecId.getProductPackageName());
        productSpec.setPremiumMax(null);
        productSpec.setPremiumMin(null);
        productSpec.setPremiumLimitsPeriodicityCode(PeriodicityCode.EVERY_MONTH);
        productSpec.setProductCurrency(currency);
        productSpec.setProductLogicName(productSpecId.getProductLogicName());
        productSpec.setSamePremiumRateAllAges(true);
        productSpec.setSamePremiumRateAllGender(true);
        productSpec.setSumInsuredMax(Amount.amount(1500000.0, currency));
        productSpec.setSumInsuredMin(Amount.amount(100000.0, currency));
        productSpec.setTaxDeductionPerYearMax(100000.0);
        return productSpec;
    }

    @Override
    public void calculateQuote(Quote quote, ProductQuotation productQuotation) {
        ProductSpec productSpec = super.getProductSpec(productQuotation);
        Instant now = Instant.now();
        super.calculateQuote(quote, productQuotation);
        calculateYearlyCashback(productSpec, now, quote, productQuotation);
    }
    //TODO Need to migrate DB.
//    protected ProductPremiumRate validateExistPremiumRate(ProductSpec productSpec, int insuredAge, GenderCode insuredGenderCode) {
//        String productId = productSpec.getProductLogicName();
//        Optional<ProductPremiumRate> iProtectRateOptional;
//        if (StringUtils.isNotBlank(productSpec.getPackageName())) {
//            iProtectRateOptional = productPremiumRateService.findPremiumRateByProductIdAndPackageName(productId, productSpec.getPackageName());
//        }
//        return iProtectRateOptional.orElseThrow(() -> QuoteCalculationException.premiumRateNotFoundException.apply(String.format("productId: %s, age: %s, gender: %s", productId, insuredAge, insuredGenderCode)));
//    }

    /**
     * @param premium
     * @param periodicityCode periodicity (MONTH, YEAR...) of premium payment.
     */
    @Override
    protected void calculateDeathBenefits(Instant now, PremiumDetail premiumDetail, int paymentYears, int coverageYears, Amount premium, PeriodicityCode periodicityCode) {
        Amount sumInsured = premiumDetail.getSumInsured();
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
        premiumDetail.setYearlyDeathBenefits(yearlyDeathBenefits);
    }

    /**
     * You must set coverage years before calling this method.
     *
     * @param quote
     * @param productQuotation
     */
    protected void calculateYearlyCashback(ProductSpec productSpec, Instant now, Quote quote, ProductQuotation productQuotation) {
        PremiumDetail premiumDetail = getPremiumDetail(quote.getPremiumsData());
        String dividendOptionId = productQuotation.getDividendOptionId();
        premiumDetail.setDividendOptionId(dividendOptionId);
        int coverageYears = quote.getCommonData().getNbOfYearsOfCoverage();
        Amount sumInsuredValue = premiumDetail.getSumInsured();

        List<DateTimeAmount> yearlyCashBackForEndOfContract = calculateYearlyCashBack(productSpec, sumInsuredValue, coverageYears, now, productSpec.getDividendInterestRateForEndOfContract());
        List<DateTimeAmount> yearlyCashBackForAnnual = calculateYearlyCashBack(productSpec, sumInsuredValue, coverageYears, now, productSpec.getDividendInterestRateForAnnual());
        premiumDetail.setYearlyCashBacksForEndOfContract(yearlyCashBackForEndOfContract);
        premiumDetail.setYearlyCashBacksForAnnual(yearlyCashBackForAnnual);

        Amount endOfContractBenefit;
        if (ProductDividendOption.END_OF_CONTRACT_PAY_BACK.getId().equals(dividendOptionId)) {
            endOfContractBenefit = ListUtil.getLastItem(yearlyCashBackForEndOfContract).getAmount();
        } else {
            endOfContractBenefit = ListUtil.getLastItem(yearlyCashBackForAnnual).getAmount();
        }
        premiumDetail.setEndOfContractBenefit(endOfContractBenefit);
    }

    private List<DateTimeAmount> calculateYearlyCashBack(ProductSpec productSpec, Amount sumInsuredValue, int coverageYears, Instant now, double dividendInterestRate) {
        Amount plainAnnualCashBackInNormalYear = sumInsuredValue.multiply(productSpec.getDividendRateInNormalYear());
        Amount plainAnnualCashBackInLastYear = sumInsuredValue.multiply(productSpec.getDividendRateInLastYear());

        List<DateTimeAmount> yearlyCashBacks = new ArrayList<>();
        Amount amountPreviousYear = new Amount(0.0, productSpec.getProductCurrency());
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

    /**
     * Abstract
     *
     * @return
     */
    @Override
    protected PremiumDetail getPremiumDetail(PremiumsData premiumsData) {
        return premiumsData.getPremiumDetail();
    }

    @Override
    protected void setConstructPremiumDetail(PremiumsData premiumsData) {
        premiumsData.setPremiumDetail(new ProductIGenPremium());
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
        if (productQuotation.getOccupationId() == null) {
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
        return true;
    }

    @Override
    protected void resetCalculatedStuff(Quote quote) {
        PremiumDetail premiumDetail = getPremiumDetail(quote.getPremiumsData());
        if (premiumDetail != null) {
            premiumDetail.setSumInsuredBeforeDiscount(null);
            premiumDetail.setSumInsured(null);
            //Don't use Collections.EMPTY_LIST here because it cannot be appended.
            premiumDetail.setYearlyDeathBenefits(new ArrayList<>());
            premiumDetail.setYearlyCashBacksForAnnual(new ArrayList<>());
            premiumDetail.setYearlyCashBacksForEndOfContract(new ArrayList<>());
            premiumDetail.setYearlyTaxDeduction(null);
            premiumDetail.setTotalTaxDeduction(null);
            premiumDetail.setEndOfContractBenefit(null);
        }
        quote.getCoverages().clear();
    }

    @Override
    protected double getDiscountRate(ProductSpec productSpec, Amount sumInsuredAmount) {
        return 0.0;//This product doesn't have discount.
    }

    @Override
    //For this product, the occupation doesn't affect the calculation, so occupationRate is always 0!
    protected double getOccupationRate(OccupationType occupationType) {
        return 0.0;
    }

}
