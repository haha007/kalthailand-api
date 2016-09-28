package th.co.krungthaiaxa.api.elife.products.igen;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductAssertUtil;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class IGenServiceTest extends ELifeTest {
    public static final ProductType PRODUCT_TYPE = ProductType.PRODUCT_IGEN;

    @Autowired
    private IGenService iGenService;

    @Autowired
    private ProductPremiumRateRepository productPremiumRateRepository;

    @Test
    public void createCreateDefaultPremiumRateIfNotExist() {
        Optional<ProductPremiumRate> productPremiumRateOptional = productPremiumRateRepository.findOneByProductId(PRODUCT_TYPE.getLogicName());
        if (!productPremiumRateOptional.isPresent()) {
            ProductPremiumRate productPremiumRate = new ProductPremiumRate();
            productPremiumRate.setProductId(PRODUCT_TYPE.getLogicName());
            productPremiumRate.setPremiumRate(308.0);
            productPremiumRateRepository.save(productPremiumRate);
        }
    }

    @Test
    public void create_product_amount_success_wiht_only_productType() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(null, null, null, null, null);
        ProductAmounts productAmounts = iGenService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithSumInsureLimits(productAmounts);
    }

    @Test
    public void create_product_amount_success_with_full_default_data() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(15, PeriodicityCode.EVERY_MONTH, 10000.0, true, 35);
        ProductAmounts productAmounts = iGenService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithFullDetail(productAmounts);
    }
}
