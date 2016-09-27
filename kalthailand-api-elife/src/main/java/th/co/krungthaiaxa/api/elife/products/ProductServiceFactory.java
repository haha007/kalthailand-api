package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.products.igen.ProductIGenService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectService;

import javax.inject.Inject;

@Component
public class ProductServiceFactory {
    private final ProductIBeginService productIBegin;
    private final ProductIFineService productIFine;
    private final ProductIGenService productIGen;
    private final IProtectService productIProtect;

    @Inject
    public ProductServiceFactory(ProductIBeginService productIBegin, ProductIFineService productIFine, ProductIGenService productIGen, IProtectService productIProtect) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = productIGen;
        this.productIProtect = productIProtect;
    }

    /**
     * @param productName get from {@link ProductType#getLogicName()}.
     * @return
     */
    public ProductService getProduct(String productName) {
        if (productName.equals(ProductType.PRODUCT_10_EC.getLogicName())) {
            return new Product10ECService();
        } else if (productName.equals(ProductType.PRODUCT_IBEGIN.getLogicName())) {
            return productIBegin;
        } else if (productName.equals(ProductType.PRODUCT_IFINE.getLogicName())) {
            return productIFine;
        } else if (productName.equals(ProductType.PRODUCT_IGEN.getLogicName())) {
            return productIGen;
        } else if (productName.equals(ProductType.PRODUCT_IPROTECT.getLogicName())) {
            return productIProtect;
        } else {
            throw new IllegalArgumentException("The Product [" + productName + "] is unknown.");
        }
    }
}
