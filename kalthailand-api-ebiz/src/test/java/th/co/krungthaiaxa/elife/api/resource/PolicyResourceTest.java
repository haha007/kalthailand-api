package th.co.krungthaiaxa.elife.api.resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.service.QuoteService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
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
@ActiveProfiles("dev")
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

    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private QuoteRepository quoteRepository;
    @Inject
    private QuoteService quoteService;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/policies");
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_creating_policy_from_none_existing_quote() throws IOException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("jsonQuote", getJSon(quote(EVERY_QUARTER, insured(25, TRUE), beneficiary(100.0))));

        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(error.getCode()).isEqualTo(POLICY_CANNOT_BE_CREATED.apply("").getCode());
    }

    @Test
    public void should_return_a_policy_object() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("jsonQuote", getJSon(quote));
        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Policy policy = TestUtil.getPolicyFromJSon(response.getBody());

        // check database values
        Policy savedPolicy = policyRepository.findOne(policy.getId());
        assertThat(savedPolicy.getId()).isNotNull();
        assertThat(savedPolicy.getPolicyId()).isNotNull();
    }

    @Test
    public void should_return_policy_payment_list() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("jsonQuote", getJSon(quote));
        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Policy policy = TestUtil.getPolicyFromJSon(response.getBody());

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getId() + "/payments");
        ResponseEntity<String> paymentResponse = template.getForEntity(paymentURI, String.class);
        List<Payment> payments = TestUtil.getPaymentsFromJSon(paymentResponse.getBody());
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(payments).hasSize(6);
    }

    @Test
    public void should_return_base64_and_also_save_pdf_format_to_file_system() {

    }
}
