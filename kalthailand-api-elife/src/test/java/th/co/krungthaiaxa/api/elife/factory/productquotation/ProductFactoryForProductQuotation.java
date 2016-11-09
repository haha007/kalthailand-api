package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.elife.products.ProductType;

/**
 * @author khoi.tran on 11/9/16.
 */
public class ProductFactoryForProductQuotation {

    public static AbstractFactoryForProductQuotation getFactory(ProductType productType) {
        if (productType == ProductType.PRODUCT_IGEN) {
            return new IGenFactoryForProductQuotation();
        } else if (productType == ProductType.PRODUCT_IPROTECT) {
            return new IProtectFactoryForProductQuotation();
        } else if (productType == ProductType.PRODUCT_IFINE) {
            return new IFineFactoryForProductQuotation();
        } else {
            throw new UnexpectedException("Not support " + productType);
        }
    }
}
