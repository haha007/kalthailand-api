package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.model.CommonData;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.PremiumsData;
import th.co.krungthaiaxa.elife.api.model.Quote;

public interface Product {
    void calculateQuote(Quote quote, ProductQuotation productQuotation);

    void getPolicyFromQuote(Policy policy, Quote quote);

    CommonData getCommonData();

    ProductAmounts getProductAmounts(ProductQuotation productQuotation);

    PremiumsData getPremiumData();
}
