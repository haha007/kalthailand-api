package th.co.krungthaiaxa.api.elife.products.igen;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IGenServiceTest extends ELifeTest {
    @Autowired
    private ProductPremiumRateRepository productPremiumRateRepository;
//
//    @Test
//    public void testCreateDefaultPremiumRate() {
//        ProductPremiumRate productPremiumRate = new ProductPremiumRate();
//        productPremiumRate.setProductId(ProductType.PRODUCT_IGEN.getLogicName());
//        productPremiumRate.setPremiumRate(308.0);
//        productPremiumRateRepository.save(productPremiumRate);
//    }
}
