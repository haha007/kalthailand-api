package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ProductFactory {
    private final ProductIBegin productIBegin;
    private final ProductIFine productIFine;
    private final ProductIGen productIGen;

    @Inject
    public ProductFactory(ProductIBegin productIBegin, ProductIFine productIFine, ProductIGen productIGen) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = productIGen;
    }

    public Product getProduct(String productId) {
        if (productId.equals(ProductType.PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productId.equals(ProductType.PRODUCT_IBEGIN.getName())) {
            return productIBegin;
        } else if (productId.equals(ProductType.PRODUCT_IFINE.getName())) {
            return productIFine;
        } else if (productId.equals(ProductType.PRODUCT_IGEN.getName())) {
            return productIGen;
        } else if (productId.equals(ProductType.PRODUCT_IPROTECT.getName())) {
            return new ProductIProtect();
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
}
