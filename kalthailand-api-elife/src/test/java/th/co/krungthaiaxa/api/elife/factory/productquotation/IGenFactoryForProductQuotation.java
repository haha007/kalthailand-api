package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/9/16.
 */
public class IGenFactoryForProductQuotation extends AbstractFactoryForProductQuotation {
    @Override
    public ProductQuotation constructDefault() {
        return ProductQuotationFactory.constructIGenDefault();
    }

    @Override
    public ProductQuotation constructDefault(PeriodicityCode periodicityCode, AtpMode atpMode) {
        return ProductQuotationFactory.constructIGen(33, periodicityCode, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH, atpMode);
    }
}
