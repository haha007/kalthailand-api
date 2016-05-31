package th.co.krungthaiaxa.api.elife.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;

import javax.inject.Inject;

import static org.springframework.http.HttpMethod.POST;

@Service
public class SigningClient {
    private final static Logger logger = LoggerFactory.getLogger(SigningClient.class);
    @Value("${kal.api.auth.username}")
    private String userName;
    @Value("${kal.api.auth.password}")
    private String userPassword;
    @Value("${kal.api.signing.url}")
    private String signingApiURL;
    @Inject
    private AuthClient authClient;
    private RestTemplate template = new RestTemplate();

    public byte[] getEncodedSignedPdfDocument(byte[] encodedNonSignedPdf) {
        HttpEntity<byte[]> entity = new HttpEntity<>(encodedNonSignedPdf, authClient.getHeadersWithToken(userName, userPassword));

        ResponseEntity<String> authResponse;
        try {
            authResponse = template.exchange(signingApiURL, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unknown error, unable to sign PDF document.", e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + authResponse.getBody() + "]");
        }

        logger.info("Got signed document from signing API");
        return authResponse.getBody().getBytes();
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
