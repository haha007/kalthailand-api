package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.products.iprotect.ProductIProtectService;

import javax.inject.Inject;

@Component
public class ProductServiceFactory {
    private final ProductIBeginService productIBegin;
    private final ProductIFineService productIFine;
    private final ProductIGenService productIGen;
    private final ProductIProtectService productIProtect;

    @Inject
    public ProductServiceFactory(ProductIBeginService productIBegin, ProductIFineService productIFine, ProductIGenService productIGen, ProductIProtectService productIProtect) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = productIGen;
        this.productIProtect = productIProtect;
    }

    /**
     * @param productName get from {@link ProductType#getName()}.
     * @return
     */
    public ProductService getProduct(String productName) {
        if (productName.equals(ProductType.PRODUCT_10_EC.getName())) {
            return new Product10ECService();
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
