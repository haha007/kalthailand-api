package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;

public interface Product {
    void calculateQuote(Quote quote, ProductQuotation productQuotation);

    void getPolicyFromQuote(Policy policy, Quote quote);

    CommonData getCommonData();

    ProductAmounts getProductAmounts(ProductQuotation productQuotation);

    PremiumsData getPremiumData();
}
