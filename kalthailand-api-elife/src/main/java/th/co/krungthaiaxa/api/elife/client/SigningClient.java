package th.co.krungthaiaxa.api.elife.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;

import java.time.Instant;

import static org.springframework.http.HttpMethod.POST;

@Service
public class SigningClient {
    private final static Logger logger = LoggerFactory.getLogger(SigningClient.class);
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${kal.api.signing.url}")
    private String signingApiURL;
    private RestTemplate template = new RestTemplate();
    public static final String PASSWORD_DOB_PATTERN = "ddMMyyyy";

    public byte[] getEncodedSignedPdfDocument(byte[] encodedNonSignedPdf, String token) {
        Instant start = LogUtil.logStarting("EncodedSignedPdf [start] URL: " + signingApiURL + ", token: " + token);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add(tokenHeader, token);

        HttpEntity<byte[]> entity = new HttpEntity<>(encodedNonSignedPdf, httpHeaders);

        ResponseEntity<String> authResponse;
        try {
            authResponse = template.exchange(signingApiURL, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unable to sign PDF document:" + e.getMessage(), e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + authResponse.getBody() + "]");
        }

        logger.info("Got signed document from signing API");
        LogUtil.logFinishing(start, "EncodedSignedPdf [finished] URL: " + signingApiURL + ", token: " + token);
        return authResponse.getBody().getBytes();
    }

    public byte[] getEncodedSignedPdfWithPassword(byte[] encodedNonSignedPdf, String password, String token) {
        Instant start = LogUtil.logStarting("EncodedSignedPdfWithPassword [start] URL: " + signingApiURL + ", token: " + token);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add(tokenHeader, token);

        HttpEntity<byte[]> entity = new HttpEntity<>(encodedNonSignedPdf, httpHeaders);

        ResponseEntity<String> signingResponse;
        try {
            signingResponse = template.exchange(signingApiURL + "/" + password, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unable to sign PDF document:" + e.getMessage(), e);
        }

        if (signingResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + signingResponse.getBody() + "]");
        }

        logger.info("Got signed document from signing API");
        LogUtil.logFinishing(start, "EncodedSignedPdfWithPassword [finished] URL: " + signingApiURL + ", token: " + token);
        return signingResponse.getBody().getBytes();
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
