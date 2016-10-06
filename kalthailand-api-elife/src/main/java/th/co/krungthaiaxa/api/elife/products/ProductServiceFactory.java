package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.products.igen.IGenQuoteCalculationService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectQuoteCalculationService;

import javax.inject.Inject;

@Component
public class ProductServiceFactory {
    private final ProductIBeginService productIBegin;
    private final ProductIFineService productIFine;
    private final IGenQuoteCalculationService productIGen;
    private final IProtectQuoteCalculationService productIProtect;

    @Inject
    public ProductServiceFactory(ProductIBeginService productIBegin, ProductIFineService productIFine, IGenQuoteCalculationService iGenService, IProtectQuoteCalculationService iProtectService) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = iGenService;
        this.productIProtect = iProtectService;
    }

    /**
     * @param productName get from {@link ProductType#getLogicName()}.
     * @return
     */
    public ProductService getProductService(String productName) {
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
