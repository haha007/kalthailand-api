package th.co.krungthaiaxa.elife.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LinePayServiceTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private LinePayService linePayService;

    @Before
    public void setup() {
        linePayService = mock(LinePayService.class);
    }

    @Test
    public void should_book_a_payment() {
        when(linePayService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        Optional<LinePayResponse> linePayBookingResponse = linePayService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        assertThat(linePayBookingResponse.isPresent()).isTrue();
        assertThat(linePayBookingResponse.get().getReturnCode()).isEqualTo("0000");
        assertThat(linePayBookingResponse.get().getReturnMessage()).isEqualTo("success");
        assertThat(linePayBookingResponse.get().getInfo().getTransactionId()).isEqualTo("123");
    }

    @Test
    public void should_confirm_a_payment() {
        when(linePayService.bookPayment(anyString(), any(), anyString(), anyString())).thenReturn(linePayResponse("0000", "success", "123"));
        when(linePayService.confirmPayment("123", 100.0, "THB")).thenReturn(linePayResponse("0000", "success"));

        Policy policy = getPolicy();
        Amount amount = policy.getPayments().get(0).getAmount();
        Optional<LinePayResponse> linePayBookingResponse = linePayService.bookPayment("123", policy, amount.getValue().toString(), amount.getCurrencyCode());
        Optional<LinePayResponse> linePayConfirmingResponse = linePayService.confirmPayment(linePayBookingResponse.get().getInfo().getTransactionId(), 100.0, "THB");
        assertThat(linePayConfirmingResponse.isPresent()).isTrue();
        assertThat(linePayConfirmingResponse.get().getReturnCode()).isEqualTo("0000");
        assertThat(linePayConfirmingResponse.get().getReturnMessage()).isEqualTo("success");
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
