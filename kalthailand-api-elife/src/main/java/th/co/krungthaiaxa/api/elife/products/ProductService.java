package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.Quote;

public interface ProductService {
    void calculateQuote(Quote quote, ProductQuotation productQuotation);

    void createPolicyFromQuote(Policy policy, Quote quote);

    CommonData initCommonData(ProductQuotation productQuotation);

    ProductAmounts calculateProductAmounts(ProductQuotation productQuotation);

    PremiumsData initPremiumData();
}
