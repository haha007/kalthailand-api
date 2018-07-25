package th.co.krungthaiaxa.api.signing.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.*;
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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;

import static java.nio.charset.Charset.forName;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiSigning.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class DocumentResourceTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentResourceTests.class);

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
    public void givenNullDocument_whenCallDocumentResource_thenReturnAnErrorDocumentIsNull()
            throws URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>(null, getHeadersWithFakeToken("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.postForEntity(uri, entity, String.class);

        Assert.assertEquals(response.getStatusCode().value(), BAD_REQUEST.value());
    }

    @Test
    public void givenNonBase64Document_whenCallDocumentResource_thenReturnAnErrorDocumentIsNotBase64Format()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>("Test document content", getHeadersWithFakeToken("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        Assert.assertEquals(response.getStatusCode().value(), NOT_ACCEPTABLE.value());
        Assert.assertEquals(error.getCode(), ErrorCode.NOT_BASE_64_ENCODED.getCode());
    }

    @Test
    public void givenNonEncodedDocument_whenCallDocumentResource_thenReturnAnErrorDocumentIsNotEncode()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>(
                new String(Base64.getEncoder().encode("Test document content".getBytes()), forName("UTF-8")),
                getHeadersWithFakeToken("NO_TOKEN")
        );

        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        Assert.assertEquals(response.getStatusCode().value(), NOT_ACCEPTABLE.value());
        Assert.assertEquals(error.getCode(), ErrorCode.PDF_INVALID.apply("").getCode());
    }

    @Test
    public void givenBase64EncodedDocument_whenCallDocumentResource_thenSignDocumentProperly()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithFakeToken("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()));
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
        Assert.assertNotNull(Base64.getDecoder().decode(response.getBody()));
    }

    @Test
    public void givenBase64EncodedDocumentWithEncryptionPassword_whenCallDocumentResource_thenSignAndEncryptDocumentWithDefaultConfiguration()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithFakeToken("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf/1234567890");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()), "1234567890".getBytes());
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
        Assert.assertNotNull(Base64.getDecoder().decode(response.getBody()));
    }

    @Test
    public void givenBase64EncodedDocumentWithContentTypeJSON_whenCallDocumentResource_thenSignDocumentWithGivenConfiguration()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        HttpEntity entity = new HttpEntity<>(getBase64("/application-form.pdf"), getHeadersWithFakeTokenForJSON("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()));
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
    }

    @Test
    public void givenJSONPayload_whenCallDocumentResource_thenSignDocumentProperly()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(new SignDocument(
                getBase64("/application-form.pdf"),
                "Test signingLocation",
                "Test signingReason",
                "KTB",
                null
        ));
        HttpEntity entity = new HttpEntity<>(payload, getHeadersWithFakeTokenForJSON("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()));
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
    }

    @Test
    public void givenJSONPayloadWithSpecificCertificate_whenCallDocumentResource_thenSignDocumentWithGivenCertificate()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(new SignDocument(
                getBase64("/application-form.pdf"),
                "Test signingLocation",
                "Test signingReason",
                "ELIFE",
                null
        ));
        HttpEntity entity = new HttpEntity<>(payload, getHeadersWithFakeTokenForJSON("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()));
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
 }

    @Test
    public void givenJSONPayloadWithNonExistingCertificate_whenCallDocumentResource_thenSignDocumentWithDefaultCertificate()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(new SignDocument(
                getBase64("/application-form.pdf"),
                "Test signingLocation",
                "Test signingReason",
                "NO_CERT",
                null
        ));
        HttpEntity entity = new HttpEntity<>(payload, getHeadersWithFakeTokenForJSON("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        Assert.assertEquals(response.getStatusCode().value(), OK.value());
    }

    @Test
    public void givenJSONPayloadWithEncryptionPassword_whenCallDocumentResource_thenSignAndEncryptDocumentWithGivenConfiguration()
            throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(new SignDocument(
                getBase64("/application-form.pdf"),
                "Test signingLocation",
                "Test signingReason",
                "ELIFE",
                "1234567890"
        ));
        HttpEntity entity = new HttpEntity<>(payload, getHeadersWithFakeTokenForJSON("NO_TOKEN"));
        URI uri = new URI("http://localhost:" + port + "/documents/signpdf");
        ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);

        new PdfReader(Base64.getDecoder().decode(response.getBody().getBytes()), "1234567890".getBytes());
        Assert.assertEquals(response.getStatusCode().value(), OK.value());
    }

    private HttpHeaders getHeadersWithFakeToken(String token) {
        HttpHeaders result = new HttpHeaders();
        result.add(tokenHeader, token);

        return result;
    }

    private HttpHeaders getHeadersWithFakeTokenForJSON(String token) {
        HttpHeaders result = getHeadersWithFakeToken(token);
        result.setContentType(MediaType.APPLICATION_JSON);

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

    class SignDocument {
        @JsonProperty("content")
        String content;
        @JsonProperty("signingLocation")
        String signingLocation;
        @JsonProperty("signingReason")
        String signingReason;
        @JsonProperty("certificate")
        String certificate;
        @JsonProperty("password")
        String password;

        private SignDocument(String content, String signingLocation, String signingReason, String certificate, String password) {
            this.content = content;
            this.signingLocation = signingLocation;
            this.signingReason = signingReason;
            this.certificate = certificate;
            this.password = password;
        }

    }

}
