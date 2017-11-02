package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.line.LinePayService;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.beneficiary;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.linePayResponse;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.quote;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LinePayServiceTest extends ELifeTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private LinePayService linePayService;

    @Before
    public void setup() {
        linePayService = Mockito.mock(LinePayService.class);
    }

    @Test
    public void should_book_a_payment() throws IOException {
        when(linePayService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        LinePayResponse linePayBookingResponse = linePayService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        assertThat(linePayBookingResponse.getReturnCode()).isEqualTo("0000");
        assertThat(linePayBookingResponse.getReturnMessage()).isEqualTo("success");
        assertThat(linePayBookingResponse.getInfo().getTransactionId()).isEqualTo("123");
    }

    @Test
    public void should_confirm_a_payment() throws IOException {
        when(linePayService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));
        when(linePayService.confirmPayment("123", 100.0, "THB")).thenReturn(linePayResponse("0000", "success"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        LinePayResponse linePayBookingResponse = linePayService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        LinePayResponse linePayConfirmingResponse = linePayService.confirmPayment(linePayBookingResponse.getInfo().getTransactionId(), 100.0, "THB");
        assertThat(linePayConfirmingResponse.getReturnCode()).isEqualTo("0000");
        assertThat(linePayConfirmingResponse.getReturnMessage()).isEqualTo("success");
    }

    @Test
    public void should_capture_a_payment() throws IOException {
        when(linePayService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));
        when(linePayService.confirmPayment("123", 100.0, "THB")).thenReturn(linePayResponse("0000", "success"));
        when(linePayService.capturePayment("123", 100.0, "THB")).thenReturn(linePayResponse("0000", "success"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        LinePayResponse linePayBookingResponse = linePayService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        LinePayResponse linePayConfirmingResponse = linePayService.confirmPayment(linePayBookingResponse.getInfo().getTransactionId(), 100.0, "THB");
        LinePayResponse linePayCaptureResponse = linePayService.capturePayment(linePayBookingResponse.getInfo().getTransactionId(), 100.0, "THB");
        assertThat(linePayConfirmingResponse.getReturnCode()).isEqualTo("0000");
        assertThat(linePayConfirmingResponse.getReturnMessage()).isEqualTo("success");
        assertThat(linePayCaptureResponse.getReturnMessage()).isEqualTo("success");
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        return policyService.createPolicy(quote);
    }
}
