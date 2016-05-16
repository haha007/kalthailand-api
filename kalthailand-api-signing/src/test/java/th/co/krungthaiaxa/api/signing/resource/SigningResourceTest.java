package th.co.krungthaiaxa.api.signing.resource;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.signing.KALApiSigning;
import th.co.krungthaiaxa.api.signing.TestUtil;
import th.co.krungthaiaxa.api.signing.model.Error;
import th.co.krungthaiaxa.api.signing.model.ErrorCode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static java.nio.charset.Charset.forName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiSigning.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class SigningResourceTest {
    @Value("${kal.api.auth.username}")
    private String userName;
    @Value("${kal.api.auth.password}")
    private String userPassword;
    @Value("${kal.api.auth.token.create.url}")
    private String createTokenUrl;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${local.server.port}")
    private int port;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate();
    }

    @Test
    public void should_return_error_when_no_document_received() throws IOException, URISyntaxException {
        HttpEntity entity = new HttpEntity<>(null, getHeadersWithToken());
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.postForEntity(base, entity, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_document_is_not_base64() throws IOException, URISyntaxException {
        HttpEntity entity = new HttpEntity<>("something", getHeadersWithToken());
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.NOT_BASE_64_ENCODED.getCode());
    }

    @Test
    public void should_return_error_when_document_is_not_encoded_pdf() throws IOException, URISyntaxException {
        HttpEntity entity = new HttpEntity<>(new String(Base64.getEncoder().encode("something".getBytes()), forName("UTF-8")), getHeadersWithToken());
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.PDF_INVALID.apply("").getCode());
    }

    @Test
    public void should_return_base_64_encoded_byte_array() throws IOException, URISyntaxException {
        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithToken());
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_pdf() throws IOException, URISyntaxException {
        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithToken());
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()))).isNotNull();
    }

    private HttpHeaders getHeadersWithToken() throws URISyntaxException {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI = new URI(createTokenUrl);
        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName)
                .queryParam("password", userPassword);
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(authURIHeaders), String.class);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Unable to create token; Response is [" + authResponse.getBody() + "]");
        }

        HttpHeaders result = new HttpHeaders();
        result.add("Content-Type", "application/json");
        result.add(tokenHeader, authResponse.getBody());

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
}
