package th.co.krungthaiaxa.elife.api.products;

public class ProductFactory {
    public static Product getProduct(String productId) {
        switch (productId) {
            case Product10EC.PRODUCT_10_EC_ID:
                return new Product10EC();
            default:
                throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
