package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class IProtectServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(IProtectServiceTest.class);
    @Inject
    QuoteService quoteService;

    @Inject
    PolicyService policyService;

    @Inject
    IProtectDiscountRateExcelLoaderService iProtectDiscountRateExcelLoaderService;

    private static Quote quote = null;

    @Test
    public void test_01_create_quote() {
        ProductQuotation productQuotation = TestUtil.productQuotation(
                ProductType.PRODUCT_IPROTECT,
                IProtectPackage.IPROTECT10.name(),
                32,
                PeriodicityCode.EVERY_MONTH,
                1000.0, false,
                35,
                GenderCode.MALE);
        quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(quote));
    }

    @Test
    public void test_02_01_update_quote() {
        if (quote == null) {
            test_01_create_quote();
        }
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
    }

    @Test
    public void test_02_02_create_policy_from_quote() {
        if (quote == null) {
            test_01_create_quote();
        }
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        Policy policy = policyService.createPolicy(quote);
        Assert.assertEquals(policy.getPolicyId(), quote.getPolicyId());
    }
}
