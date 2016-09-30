package th.co.krungthaiaxa.api.elife.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.assertj.core.api.Assertions;
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
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class PolicyResourceTest extends ELifeTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
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
        template = new TestRestTemplate();
    }

    @Test
    public void should_return_error_when_creating_policy_from_none_existing_quote() throws IOException {
        String jsonQuote = TestUtil.getJSon(TestUtil.quote(TestUtil.product10ECService()));
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(jsonQuote), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED.getCode());
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
        List<Payment> payments = TestUtil.getPaymentsFromJSon(paymentResponse.getBody());
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(payments).hasSize(6);
    }

    @Test
    public void should_update_payment_transaction_id_and_registration_key_but_not_the_status() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("registrationKey", "myRegistrationKey")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());

        Policy updatedPolicy = TestUtil.getPolicyFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        Assertions.assertThat(updatedPolicy.getPayments().get(0).getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);
        assertThat(updatedPolicy.getPayments().get(0).getTransactionId()).isEqualTo("myTransactionId");
        //assertThat(updatedPolicy.getPayments().get(0).getRegistrationKey()).isEqualTo("myRegistrationKey");
    }

    @Test
    public void should_be_able_to_update_payment_with_error_message() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("registrationKey", "myRegistrationKey")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());

        Policy updatedPolicy = TestUtil.getPolicyFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        Assertions.assertThat(updatedPolicy.getPayments().get(0).getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);
    }

    private Policy getPolicy() throws IOException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation(35, PeriodicityCode.EVERY_YEAR));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(TestUtil.getJSon(quote)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        return TestUtil.getPolicyFromJSon(response.getBody());
    }

}
