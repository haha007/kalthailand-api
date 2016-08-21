package th.co.krungthaiaxa.api.elife.products.iprotect;

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
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IProtectServiceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(IProtectServiceTest.class);
    @Inject
    private QuoteService quoteService;

    @Inject
    IProtectDiscountRateExcelLoaderService iProtectDiscountRateExcelLoaderService;

    @Test
    public void create_quote() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation(ProductType.PRODUCT_IPROTECT, IProtectPackage.IPROTECT10.name(), 32, PeriodicityCode.EVERY_MONTH, 1000.0, false, 35, GenderCode.MALE));
//        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(quote));
//        quote = quoteService.updateQuote(quote, "token");

    }

}
