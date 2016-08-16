package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIProtectRateLoaderServiceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductIProtectRateLoaderServiceTest.class);
    @Inject
    ProductIProtectRateLoaderService productIProtectRateLoaderService;

    @Test
    public void loadPredefinedRates() {
        List<IprotectPredefinedRate> productIProtectRates = productIProtectRateLoaderService.getPredefinedIProtectRates(ProductIProtectPackage.IPROTECT10);
        LOGGER.trace(ObjectMapperUtil.toStringMultiLineForEachElement(productIProtectRates));

        Assert.assertTrue(!productIProtectRates.isEmpty());
        for (IprotectPredefinedRate productIProtectRate : productIProtectRates) {
            Assert.assertTrue(productIProtectRate.getAge() >= 0);
            Assert.assertTrue(productIProtectRate.getFemaleRate() > 0);
            Assert.assertTrue(productIProtectRate.getMaleRate() > 0);
        }
    }
}
