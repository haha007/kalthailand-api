package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.beneficiary;
import static th.co.krungthaiaxa.elife.api.TestUtil.productQuotation;
import static th.co.krungthaiaxa.elife.api.TestUtil.quote;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;

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
    public void should_return_0_when_sending_comfirmation_message_successfully(){
        Map<String,String> m;
        try {
            Policy pol = getPolicy();
            pol.getInsureds().get(0).getPerson().getMobilePhoneNumber().setNumber("0863878803");
            pol.setPolicyId("555-55555555");
            m = smsApiService.sendConfirmationMessage(pol);
            assertThat(m.get("STATUS")).contains("0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        return policyService.createPolicy(quote);
    }

}
