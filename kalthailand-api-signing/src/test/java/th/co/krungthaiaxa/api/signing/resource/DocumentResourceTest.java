package th.co.krungthaiaxa.api.signing.resource;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.signing.KALApiSigning;
import th.co.krungthaiaxa.api.signing.TestUtil;
import th.co.krungthaiaxa.api.signing.filter.KalApiTokenFilter;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static java.nio.charset.Charset.forName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiSigning.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public class DocumentResourceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentResourceTest.class);

    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${local.server.port}")
    private int port;
    @Inject
    private KalApiTokenFilter kalApiTokenFilter;
    private TestRestTemplate restTemplate;
    private RestTemplate fakeAuthRestTemplate;

    @Before
    public void setup() {
        restTemplate = new TestRestTemplate();
        fakeAuthRestTemplate = mock(RestTemplate.class);
        kalApiTokenFilter.setTemplate(fakeAuthRestTemplate);
    }

    @Test
    public void should_return_error_when_no_document_received() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>(null, getHeadersWithFakeToken("123"));
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.postForEntity(base, entity, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_document_is_not_base64() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>("something", getHeadersWithFakeToken("123"));
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(base, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.NOT_BASE_64_ENCODED.getCode());
    }

    @Test
    public void should_return_error_when_document_is_not_encoded_pdf() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>(new String(Base64.getEncoder().encode("something".getBytes()), forName("UTF-8")), getHeadersWithFakeToken("123"));
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(base, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.PDF_INVALID.apply("").getCode());
    }

    @Test
    public void should_return_base_64_encoded_byte_array() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithFakeToken("123"));
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(base, POST, entity, String.class);
        LOGGER.debug("Response: " + response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_pdf() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithFakeToken("123"));
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(base, POST, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()))).isNotNull();
    }

    private HttpHeaders getHeadersWithFakeToken(String token) throws URISyntaxException {
        HttpHeaders result = new HttpHeaders();
        result.add("Content-Type", "application/json");
        result.add(tokenHeader, token);

        return result;
    }

    private String getBase64(String file) {
        try {
            byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream(file));
            return new String(Base64.getEncoder().encode(content), forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }

    private ResponseEntity<String> getAuthResponseOk() {
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
