package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by santilik on 3/15/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SMSApiServiceTest {

    @Inject
    private SMSApiService smsApiService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_return_0_when_sending_comfirmation_message_successfully() throws Exception {
        Map<String, String> m = new HashMap<>();
        Policy pol = getPolicy();
        pol.getInsureds().get(0).getPerson().getMobilePhoneNumber().setNumber("0863878803");
        pol.setPolicyId("555-55555555");

        String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-booked-sms.txt"), Charset.forName("UTF-8"));
        smsApiService.sendConfirmationMessage(pol, smsContent.replace("%FULL_NAME%", pol.getInsureds().get(0).getPerson().getGivenName() + " " + pol.getInsureds().get(0).getPerson().getSurName()).replace("%POLICY_ID%", pol.getPolicyId()));

        smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-purchased-sms.txt"), Charset.forName("UTF-8"));
        smsApiService.sendConfirmationMessage(pol, smsContent));

        smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/user-not-responging-sms.txt"), Charset.forName("UTF-8"));
        m = smsApiService.sendConfirmationMessage(pol, smsContent.replace("%POLICY_ID%", pol.getPolicyId()));

        assertThat(m.get("STATUS")).contains("0");
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation());
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        return policyService.createPolicy(quote);
    }

}
