package th.co.krungthaiaxa.elife.api.resource;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.service.QuoteService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.products.Product10EC.getCommonData;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class DocumentResourceTest {
    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private RestTemplate template;
    @Inject
    private QuoteService quoteService;

    @Value("${path.store.watermarked.image}")
    private String storePath;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_policy_unknown() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/123456789/thai/id");
        ResponseEntity<String> response = template.postForEntity(base, null, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_base64image_is_not_base64() throws QuoteCalculationException, IOException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = getPolicy(quote);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>("something"), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_READABLE.getCode());
    }

    @Test
    public void should_return_error_when_image_is_too_small() throws IOException, QuoteCalculationException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = getPolicy(quote);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/small.png")), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_TOO_SMALL.getCode());
    }

    @Test
    public void should_return_error_when_sending_a_file_that_is_not_an_image() throws IOException, QuoteCalculationException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = getPolicy(quote);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/texts/sampleTextFile.txt")), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(UNSUPPORTED_MEDIA_TYPE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_SUPPORTED.getCode());
    }

    @Test
    public void should_return_ok_with_png() throws IOException, QuoteCalculationException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = getPolicy(quote);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/image1.jpg")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_ok_with_jpg() throws IOException, QuoteCalculationException, URISyntaxException {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = getPolicy(quote);

        URI documentUploadURI = new URI("http://localhost:" + port + "/documents/" + policy.getPolicyId() + "/thai/id");
        ResponseEntity<String> response = template.exchange(documentUploadURI, POST, new HttpEntity<>(getBase64("/images/image2.png")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    private Policy getPolicy(Quote quote) throws URISyntaxException, IOException {
        URI quoteCreationURI = new URI("http://localhost:" + port + "/policies");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("jsonQuote", getJSon(quote));
        ResponseEntity<String> response = template.postForEntity(quoteCreationURI, parameters, String.class);
        return TestUtil.getPolicyFromJSon(response.getBody());
    }

    private String getBase64(String file) {
        try {
            byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream(file));
            return new String(Base64.getEncoder().encode(content));
        } catch (IOException e) {
            return null;
        }
    }
}
