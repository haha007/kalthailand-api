package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.data.IProtectDiscountRate;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.OccupationType;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.product.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.product.ProductSpec;
import th.co.krungthaiaxa.api.elife.model.product.ProductSpecId;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.AbstractQuoteCalculationService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.AmountUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

@Service
public class IProtectQuoteCalculationService extends AbstractQuoteCalculationService implements ProductService {

    @Inject
    private IProtectDiscountRateService iProtectDiscountRateService;

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

    @Override
    protected ProductIProtectPremium getPremiumDetail(PremiumsData premiumsData) {
        return premiumsData.getProductIProtectPremium();
    }

    @Override
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
