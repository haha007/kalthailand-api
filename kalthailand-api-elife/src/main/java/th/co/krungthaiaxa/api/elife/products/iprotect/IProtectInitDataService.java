package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
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
        for (IProtectPackage iProtectPackage : IProtectPackage.values()) {
            List<ProductPremiumRate> productPremiumRatesInExcel = iProtectRateExcelLoaderService.excelToProductPremiumRates(iProtectPackage);
            List<ProductPremiumRate> productPremiumRatesInDB = productPremiumRateRepository.findByProductIdAndPackageName(productPremiumRatesInExcel.get(0).getProductId(), productPremiumRatesInExcel.get(0).getPackageName());
            if (productPremiumRatesInDB.isEmpty()) {
                productPremiumRateRepository.save(productPremiumRatesInExcel);
            }
        }
    }
}
