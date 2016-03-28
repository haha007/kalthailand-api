package th.co.krungthaiaxa.elife.api.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;
import th.co.krungthaiaxa.elife.api.service.QuoteService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.*;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.NOT_PROCESSED;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.PAYMENT_NOT_UPDATED_ERROR_DETAILS_NEEDED;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class PolicyResourceTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private QuoteService quoteService;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/policies");
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_creating_policy_from_none_existing_quote() throws IOException {
        String jsonQuote = getJSon(quote(product10EC()));
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(jsonQuote), String.class);

        Error error = getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED.getCode());
    }

    @Test
    public void should_return_a_policy_object() throws IOException {
        Policy policy = getPolicy();

        // check database values
        Policy savedPolicy = policyRepository.findOne(policy.getId());
        assertThat(savedPolicy.getId()).isNotNull();
        assertThat(savedPolicy.getPolicyId()).isNotNull();
    }

    @Test
    public void should_return_policy_payment_list() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/payments");
        ResponseEntity<String> paymentResponse = template.getForEntity(paymentURI, String.class);
        List<Payment> payments = getPaymentsFromJSon(paymentResponse.getBody());
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(payments).hasSize(6);
    }

    @Test
    public void should_be_able_to_update_payment_without_error_message() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("value", 200.0)
                .queryParam("currencyCode", "THB")
                .queryParam("registrationKey", "something")
                .queryParam("status", SUCCESS)
                .queryParam("channelType", LINE)
                .queryParam("creditCardName", "myCreditCardName")
                .queryParam("paymentMethod", "myPaymentMethod");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());

        Policy updatedPolicy = getPolicyFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(updatedPolicy.getPayments().get(0).getStatus()).isNotEqualTo(NOT_PROCESSED);
    }

    @Test
    public void should_not_be_able_to_update_payment_with_error_message_and_no_error_code() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("value", 200.0)
                .queryParam("currencyCode", "THB")
                .queryParam("registrationKey", "something")
                .queryParam("status", ERROR)
                .queryParam("channelType", LINE)
                .queryParam("creditCardName", "myCreditCardName")
                .queryParam("paymentMethod", "myPaymentMethod")
                .queryParam("errorMessage", "myErrorMessage");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);

        Error error = getErrorFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(PAYMENT_NOT_UPDATED_ERROR_DETAILS_NEEDED.getCode());
        assertThat(error.getCode()).isNotEqualTo(NOT_PROCESSED);
    }

    @Test
    public void should_be_able_to_update_payment_with_error_message() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("value", 200.0)
                .queryParam("currencyCode", "THB")
                .queryParam("registrationKey", "something")
                .queryParam("status", ERROR)
                .queryParam("channelType", LINE)
                .queryParam("creditCardName", "myCreditCardName")
                .queryParam("paymentMethod", "myPaymentMethod")
                .queryParam("errorCode", "myErrorCode")
                .queryParam("errorMessage", "myErrorMessage");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());

        Policy updatedPolicy = getPolicyFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(updatedPolicy.getPayments().get(0).getStatus()).isEqualTo(NOT_PROCESSED);
    }

    private Policy getPolicy() throws IOException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, LINE, productQuotation(35, EVERY_YEAR));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(getJSon(quote)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        return getPolicyFromJSon(response.getBody());
    }

}
