package th.co.krungthaiaxa.elife.api.resource;

import org.junit.Before;
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
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.products.Product10EC;
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
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_QUARTER;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.POLICY_CANNOT_BE_CREATED;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class PolicyResourceTest {
    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;
    private Product10EC product10EC = new Product10EC();

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
        String jsonQuote = getJSon(quote(EVERY_QUARTER, insured(25), beneficiary(100.0)));
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base);
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(jsonQuote), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(error.getCode()).isEqualTo(POLICY_CANNOT_BE_CREATED.apply("").getCode());
    }

    @Test
    public void should_return_a_policy_object() throws QuoteCalculationException, IOException {
        Quote quote = quoteService.createQuote(randomNumeric(20), product10EC.getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base);
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(getJSon(quote)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Policy policy = TestUtil.getPolicyFromJSon(response.getBody());

        // check database values
        Policy savedPolicy = policyRepository.findOne(policy.getId());
        assertThat(savedPolicy.getId()).isNotNull();
        assertThat(savedPolicy.getPolicyId()).isNotNull();
    }

    @Test
    public void should_return_policy_payment_list() throws QuoteCalculationException, IOException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), product10EC.getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base);
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(getJSon(quote)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Policy policy = TestUtil.getPolicyFromJSon(response.getBody());

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/payments");
        ResponseEntity<String> paymentResponse = template.getForEntity(paymentURI, String.class);
        List<Payment> payments = TestUtil.getPaymentsFromJSon(paymentResponse.getBody());
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(payments).hasSize(6);
    }

    @Test
    public void should_return_base64_and_also_save_pdf_format_to_file_system() {

    }
}
