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
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IProtectRateLoaderServiceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(IProtectRateLoaderServiceTest.class);
    @Inject
    IProtectRateExcelLoaderService iProtectRateLoaderService;

    @Inject
    IProtectRateService iProtectRateService;

    @Inject
    IProtectDiscountRateExcelLoaderService iProtectDiscountRateExcelLoaderService;

    @Test
    public void save_premium_rates_from_excel_to_db() {
        List<IProtectRate> iprotectRates = iProtectRateLoaderService.saveIProtectRatesFromExcelToDB();

        LOGGER.trace(ObjectMapperUtil.toStringMultiLineForEachElement(iprotectRates));

        Assert.assertTrue(!iprotectRates.isEmpty());
        for (IProtectRate iprotectRate : iprotectRates) {
            Assert.assertTrue(iprotectRate.getAge() >= 0);
            Assert.assertTrue(iprotectRate.getPremiumRate() > 0);
            Assert.assertTrue(iprotectRate.getGender() != null);
        }
    }

    @Test
    public void load_premium_rates_from_excel_to_db() {
        Optional<IProtectRate> iprotectRate = iProtectRateService.findIProtectRates(IProtectPackage.IPROTECT10, 35, GenderCode.MALE);

        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(iprotectRate));

        Assert.assertNotNull(iprotectRate);
    }

    @Test
    public void save_discount_rate_from_excel_to_db() {
        List<IProtectDiscountRate> iProtectDiscountRates = iProtectDiscountRateExcelLoaderService.saveIProtectDiscountRatesFromExcelToDB();

        LOGGER.trace(ObjectMapperUtil.toStringMultiLineForEachElement(iProtectDiscountRates));
        Assert.assertTrue(!iProtectDiscountRates.isEmpty());
        for (IProtectDiscountRate iProtectDiscountRate : iProtectDiscountRates) {
            Assert.assertTrue(iProtectDiscountRate.getSumInsured() >= 0);
            Assert.assertTrue(iProtectDiscountRate.getPackageName() != null);
        }
    }
}