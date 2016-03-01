package th.co.krungthaiaxa.elife.api.products;

import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;

public class ProductFactory {
    public static Product getProduct(String productId) {
        if (productId.equals(PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
