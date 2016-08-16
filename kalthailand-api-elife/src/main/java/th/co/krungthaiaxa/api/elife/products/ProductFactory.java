package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ProductFactory {
    private final ProductIBegin productIBegin;
    private final ProductIFine productIFine;
    private final ProductIGen productIGen;
    private final ProductIProtect productIProtect;

    @Inject
    public ProductFactory(ProductIBegin productIBegin, ProductIFine productIFine, ProductIGen productIGen, ProductIProtect productIProtect) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = productIGen;
        this.productIProtect = productIProtect;
    }

    /**
     * @param productName get from {@link ProductType#getName()}.
     * @return
     */
    public Product getProduct(String productName) {
        if (productName.equals(ProductType.PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productName.equals(ProductType.PRODUCT_IBEGIN.getName())) {
            return productIBegin;
        } else if (productName.equals(ProductType.PRODUCT_IFINE.getName())) {
            return productIFine;
        } else if (productName.equals(ProductType.PRODUCT_IGEN.getName())) {
            return productIGen;
        } else if (productName.equals(ProductType.PRODUCT_IPROTECT.getName())) {
            return productIProtect;
        } else {
            throw new IllegalArgumentException("The Product [" + productName + "] is unknown.");
        }
    }
}
