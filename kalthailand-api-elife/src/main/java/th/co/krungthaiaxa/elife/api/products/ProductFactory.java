package th.co.krungthaiaxa.elife.api.products;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IBEGIN;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

@Component
public class ProductFactory {
    private final ProductIFine productIFine;

    @Inject
    public ProductFactory(ProductIFine productIFine) {
        this.productIFine = productIFine;
    }

    public Product getProduct(String productId) {
        if (productId.equals(PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productId.equals(PRODUCT_IBEGIN.getName())) {
            return new ProductIBegin();
        } else if (productId.equals(PRODUCT_IFINE.getName())) {
            return productIFine;
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
