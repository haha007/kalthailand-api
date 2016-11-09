package th.co.krungthaiaxa.api.elife.test.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.IOUtils;
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
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.Charset.forName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public class DocumentResourceTest extends ELifeTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${local.server.port}")
    private int port;
    private RestTemplate template;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyFactory policyFactory;
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private PolicyService policyService;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate();
    }

    @Test
    public void should_return_error_when_policy_unknown() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/policies/123456789/thai/id");
        ResponseEntity<String> response = template.postForEntity(base, null, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_base64image_is_not_base64() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>("something"), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_READABLE.getCode());
    }

    @Test
    public void should_return_error_when_image_is_too_small() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/small.png")), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_TOO_SMALL.getCode());
    }

    @Test
    public void should_return_error_when_sending_a_file_that_is_not_an_image() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/texts/sampleTextFile.txt")), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(UNSUPPORTED_MEDIA_TYPE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_SUPPORTED.getCode());
    }

    @Test
    public void should_return_ok_with_png() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/image1.jpg")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_ok_with_jpg() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/image2.png")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_not_found_when_trying_to_list_documents_of_a_policy_that_does_not_exist() throws IOException, URISyntaxException {
        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/12345678901234567890");
        ResponseEntity<String> response = template.getForEntity(documentUploadURI, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.POLICY_DOES_NOT_EXIST.getCode());
    }

    @Test
    public void should_return_not_found_when_trying_to_download_a_document_of_a_policy_that_does_not_exist() throws IOException, URISyntaxException {
        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/12345678901234567890/1234567890");
        ResponseEntity<String> response = template.getForEntity(documentUploadURI, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.POLICY_DOES_NOT_EXIST.getCode());
    }

    @Test
    public void should_return_error_when_trying_to_download_a_document_that_does_not_exist() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId() + "/1234567890");
        ResponseEntity<String> response = template.getForEntity(documentUploadURI, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.POLICY_DOES_NOT_CONTAIN_DOCUMENT.getCode());
    }

    @Test
    public void should_return_0_documents_after_a_policy_has_been_created_and_payment_request_returned_error() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId");
        ResponseEntity<String> paymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        Policy updatedPolicy = TestUtil.getPolicyFromJSon(paymentResponse.getBody());
        Assertions.assertThat(updatedPolicy.getStatus()).isEqualTo(PolicyStatus.PENDING_PAYMENT);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + updatedPolicy.getPolicyId());
        ResponseEntity<String> response = template.getForEntity(documentUploadURI, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());

        List<Document> documents = TestUtil.getDocumentsFromJSon(response.getBody());
        assertThat(documents).hasSize(0);
    }

    @Test
    public void should_return_2_document_when_policy_monthly() throws IOException, URISyntaxException {
        Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment());

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("regKey", "myRegistrationKey")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> paymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        Policy updatedPolicy = TestUtil.getPolicyFromJSon(paymentResponse.getBody());
        Assertions.assertThat(updatedPolicy.getStatus()).isEqualTo(PolicyStatus.PENDING_VALIDATION);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/policies/" + policy.getPolicyId());
        ResponseEntity<String> response = template.getForEntity(documentUploadURI, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());

        List<Document> documents = TestUtil.getDocumentsFromJSon(response.getBody());
        assertThat(documents).hasSize(2);
    }

    private Policy getPolicy() throws URISyntaxException, IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createQuote(ProductQuotationFactory.constructIGenDefault(), TestUtil.DUMMY_EMAIL);
        Quote quote = quoteResult.getQuote();

        URI quoteCreationURI = new URI("http://localhost:" + port + "/policies");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(quoteCreationURI)
                .queryParam("sessionId", quoteResult.getSessionId())
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(TestUtil.getJSon(quote)), String.class);
        return TestUtil.getPolicyFromJSon(response.getBody());
    }

    private String getBase64(String file) {
        try {
            byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream(file));
            return new String(Base64.getEncoder().encode(content), forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }
}
