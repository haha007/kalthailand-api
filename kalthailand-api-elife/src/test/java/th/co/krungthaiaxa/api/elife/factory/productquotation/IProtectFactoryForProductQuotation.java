package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/9/16.
 */
public class IProtectFactoryForProductQuotation extends AbstractFactoryForProductQuotation {
    @Override
    public ProductQuotation constructDefault() {
        return ProductQuotationFactory.constructIProtectDefault();
    }

    @Override
    public ProductQuotation constructDefault(PeriodicityCode periodicityCode, AtpMode atpMode) {
        return ProductQuotationFactory.constructIProtect(33, periodicityCode, 300000.0, true, 35, GenderCode.MALE, atpMode);
    }

//    @Override
//    public ProductQuotation constructDefault(double paymentAmount, boolean isSumInsured, PeriodicityCode periodicityCode, AtpMode atpMode) {
//        return ProductQuotationFactory.constructIProtect(33, periodicityCode, paymentAmount, isSumInsured, 35, GenderCode.MALE, atpMode);
//    }
}
