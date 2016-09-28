package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.products.igen.IGenService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectService;

import javax.inject.Inject;

@Component
public class ProductServiceFactory {
    private final ProductIBeginService productIBegin;
    private final ProductIFineService productIFine;
    private final IGenService productIGen;
    private final IProtectService productIProtect;

    @Inject
    public ProductServiceFactory(ProductIBeginService productIBegin, ProductIFineService productIFine, IGenService iGenService, IProtectService iProtectService) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = iGenService;
        this.productIProtect = iProtectService;
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
