package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;

import java.util.List;

/**
 * @author khoi.tran on 9/30/16.
 */
@Service
public class IProtectInitDataService {
    private final IProtectRateExcelLoaderService iProtectRateExcelLoaderService;
    private final ProductPremiumRateRepository productPremiumRateRepository;

    @Autowired
    public IProtectInitDataService(IProtectRateExcelLoaderService iProtectRateExcelLoaderService, ProductPremiumRateRepository productPremiumRateRepository) {
        this.iProtectRateExcelLoaderService = iProtectRateExcelLoaderService;
        this.productPremiumRateRepository = productPremiumRateRepository;
    }

    public void initData() {
        initPremiumRate();
    }

    private void initPremiumRate() {
        String productLogicName = ProductType.PRODUCT_IPROTECT.getLogicName();
        for (IProtectPackage iProtectPackage : IProtectPackage.values()) {
            List<ProductPremiumRate> productPremiumRatesInDB = productPremiumRateRepository.findByProductIdAndPackageName(productLogicName, iProtectPackage.name());
            if (productPremiumRatesInDB.isEmpty()) {
                List<ProductPremiumRate> productPremiumRatesInExcel = iProtectRateExcelLoaderService.excelToIProtectProductPremiumRates(iProtectPackage);
                productPremiumRateRepository.save(productPremiumRatesInExcel);
            }
        }
    }
}
