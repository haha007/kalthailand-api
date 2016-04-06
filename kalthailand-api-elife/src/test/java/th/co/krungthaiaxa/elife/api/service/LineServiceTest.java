package th.co.krungthaiaxa.elife.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.line.LinePayResponse;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineServiceTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private LineService lineService;

    @Before
    public void setup() {
        lineService = Mockito.mock(LineService.class);
    }

    @Test
    public void should_book_a_payment() throws IOException {
        when(lineService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        LinePayResponse linePayBookingResponse = lineService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        assertThat(linePayBookingResponse.getReturnCode()).isEqualTo("0000");
        assertThat(linePayBookingResponse.getReturnMessage()).isEqualTo("success");
        assertThat(linePayBookingResponse.getInfo().getTransactionId()).isEqualTo("123");
    }

    @Test
    public void should_confirm_a_payment() throws IOException {
        when(lineService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));
        when(lineService.confirmPayment("123", 100.0, "THB")).thenReturn(linePayResponse("0000", "success"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        LinePayResponse linePayBookingResponse = lineService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        LinePayResponse linePayConfirmingResponse = lineService.confirmPayment(linePayBookingResponse.getInfo().getTransactionId(), 100.0, "THB");
        assertThat(linePayConfirmingResponse.getReturnCode()).isEqualTo("0000");
        assertThat(linePayConfirmingResponse.getReturnMessage()).isEqualTo("success");
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
