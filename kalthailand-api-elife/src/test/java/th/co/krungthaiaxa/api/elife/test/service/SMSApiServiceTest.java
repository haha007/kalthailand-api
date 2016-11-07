package th.co.krungthaiaxa.api.elife.test.service;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SMSApiService;

import javax.inject.Inject;
import java.nio.charset.Charset;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * Created by santilik on 3/15/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SMSApiServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(SMSApiService.class);
    @Inject
    private PolicyService policyService;
    @Inject
    private SMSApiService smsApiService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_return_0_when_sending_comfirmation_message_successfully() throws Exception {
        Policy pol = getPolicy();
        pol.getInsureds().get(0).getPerson().getMobilePhoneNumber().setNumber("0863878803");
        pol.setPolicyId("555-55555555");

        String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-booked-sms.txt"), Charset.forName("UTF-8"));
        smsApiService.sendConfirmationMessage(pol, smsContent.replace("%FULL_NAME%", pol.getInsureds().get(0).getPerson().getGivenName() + " " + pol.getInsureds().get(0).getPerson().getSurName()).replace("%POLICY_ID%", pol.getPolicyId()));

        smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-purchased-sms.txt"), Charset.forName("UTF-8"));
        smsApiService.sendConfirmationMessage(pol, smsContent);

        smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/user-not-responging-sms.txt"), Charset.forName("UTF-8"));
        SMSResponse m = smsApiService.sendConfirmationMessage(pol, smsContent.replace("%POLICY_ID%", pol.getPolicyId()));

        Assert.assertEquals(SMSResponse.STATUS_SUCCESS, m.getStatus());
    }

    @Test
    public void should_sending_message_successfully() throws Exception {
        SMSResponse response = smsApiService.sendMessageNoCatchException("0863878803", "Testing message");
        LOGGER.debug("SMS Result: " + response);
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation());
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        return policyService.createPolicy(quote);
    }

}
