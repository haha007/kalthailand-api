package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.CommonData;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.PremiumsData;
import th.co.krungthaiaxa.elife.api.model.Quote;

public interface Product {
    void calculateQuote(Quote quote, ProductQuotation productQuotation) throws QuoteCalculationException;

    void getPolicyFromQuote(Policy policy, Quote quote) throws QuoteCalculationException;

    CommonData getCommonData();

    ProductAmounts getProductAmounts(ProductQuotation productQuotation);

    PremiumsData getPremiumData();
}
