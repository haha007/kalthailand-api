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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
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
    @Value("${security.api.user.name}")
    private String apiUserName;
    @Value("${security.api.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_no_document_received() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.postForEntity(base, null, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_document_is_not_base64() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, new HttpEntity<>("something"), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.NOT_BASE_64_ENCODED.getCode());
    }

    @Test
    public void should_return_error_when_document_is_not_encoded_pdf() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, new HttpEntity<>(new String(Base64.getEncoder().encode("something".getBytes()), forName("UTF-8"))), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.PDF_INVALID.apply("").getCode());
    }

    @Test
    public void should_return_base_64_encoded_byte_array() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, new HttpEntity<>(getBase64("/application-form.pdf")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    @Test
    public void should_return_pdf() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = template.exchange(base, POST, new HttpEntity<>(getBase64("/application-form.pdf")), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()))).isNotNull();
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
