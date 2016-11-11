package th.co.krungthaiaxa.api.elife.products.utils;

import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/8/16.
 */
public class ProductQuotationUtils {
    /**
     * @param productQuotation
     * @return atpMode if is valid!
     */
    public static Integer validateAtpMode(ProductQuotation productQuotation) {
        Integer atpMode = productQuotation.getAtpMode();
        if (!ProductUtils.isAtpModeEnable(atpMode)) {
            PeriodicityCode periodicityCode = productQuotation.getPeriodicityCode();
            if (PeriodicityCode.EVERY_MONTH.equals(periodicityCode)) {
                throw new QuoteCalculationException("The policy has payment mode is Monthly, but AtpMode is not enable. You must set it as enable! \nProductQuotation: " + ObjectMapperUtil.toStringMultiLine(productQuotation));
            }
        }
        return atpMode;
    }

    /**
     * This method will validate the atpMode in productionQuotation first. If it's valid, then set to quote.
     *
     * @param quote
     * @param productQuotation
     * @return atpMode
     */
    public static Integer setValidAtpModeToQuote(Quote quote, ProductQuotation productQuotation) {
        Integer atpMode = validateAtpMode(productQuotation);
        quote.getPremiumsData().getFinancialScheduler().setAtpMode(atpMode);
        return atpMode;
    }
}