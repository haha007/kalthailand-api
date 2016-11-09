package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/9/16.
 */
public class IFineFactoryForProductQuotation extends AbstractFactoryForProductQuotation {
    @Override
    public ProductQuotation constructDefault() {
        return ProductQuotationFactory.constructIFineDefault();
    }

    @Override
    public ProductQuotation constructDefault(PeriodicityCode periodicityCode, AtpMode atpMode) {
        return ProductQuotationFactory.constructIFine(ProductIFinePackage.IFINE1, 33, periodicityCode, GenderCode.MALE, true, atpMode);
    }

}
