package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;

public interface ProductService {
    void calculateQuote(Quote quote, ProductQuotation productQuotation);

    void getPolicyFromQuote(Policy policy, Quote quote);

    CommonData initCommonData();

    ProductAmounts calculateProductAmounts(ProductQuotation productQuotation);

    PremiumsData initPremiumData();
}
