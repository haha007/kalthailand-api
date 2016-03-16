package th.co.krungthaiaxa.elife.api.products;

import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IBEGIN;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

public class ProductFactory {
    public static Product getProduct(String productId) {
        if (productId.equals(PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productId.equals(PRODUCT_IBEGIN.getName())) {
            return new ProductIBegin();
        } else if (productId.equals(PRODUCT_IFINE.getName())) {
            return new ProductIFine();
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
