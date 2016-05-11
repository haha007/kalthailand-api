package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ProductFactory {
    private final ProductIBegin productIBegin;
    private final ProductIFine productIFine;

    @Inject
    public ProductFactory(ProductIBegin productIBegin, ProductIFine productIFine) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
    }

    public Product getProduct(String productId) {
        if (productId.equals(ProductType.PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productId.equals(ProductType.PRODUCT_IBEGIN.getName())) {
            return productIBegin;
        } else if (productId.equals(ProductType.PRODUCT_IFINE.getName())) {
            return productIFine;
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
