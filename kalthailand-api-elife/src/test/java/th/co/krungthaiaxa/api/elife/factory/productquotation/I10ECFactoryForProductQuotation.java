package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/9/16.
 */
public class I10ECFactoryForProductQuotation extends AbstractFactoryForProductQuotation {
    @Override
    public ProductQuotation constructDefault() {
        return ProductQuotationFactory.construct10ECDefault();
    }

    @Override
    public ProductQuotation constructDefault(PeriodicityCode periodicityCode, AtpMode atpMode) {
        return ProductQuotationFactory.construct10ECDefault(32, periodicityCode, 10000.0, false, 35, atpMode);
    }

}
