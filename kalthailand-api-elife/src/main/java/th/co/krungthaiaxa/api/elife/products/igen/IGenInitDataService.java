package th.co.krungthaiaxa.api.elife.products.igen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;

import java.util.List;

/**
 * @author khoi.tran on 9/30/16.
 */
@Service
public class IGenInitDataService {
    private final ProductPremiumRateRepository productPremiumRateRepository;

    @Autowired
    public IGenInitDataService(ProductPremiumRateRepository productPremiumRateRepository) {
        this.productPremiumRateRepository = productPremiumRateRepository;
    }

    public void initData() {
        initPremiumRate();
    }

    private void initPremiumRate() {
        String productLogicName = ProductType.PRODUCT_IGEN.getLogicName();
        List<ProductPremiumRate> productPremiumRatesInDB = productPremiumRateRepository.findByProductId(ProductType.PRODUCT_IGEN.getLogicName());
        if (productPremiumRatesInDB.isEmpty()) {
            ProductPremiumRate productPremiumRate = new ProductPremiumRate();
            productPremiumRate.setProductId(productLogicName);
            productPremiumRate.setPremiumRate(308.0);
            productPremiumRateRepository.save(productPremiumRate);
        }
    }
}
