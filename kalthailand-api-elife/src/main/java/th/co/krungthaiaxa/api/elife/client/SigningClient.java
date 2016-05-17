package th.co.krungthaiaxa.api.elife.client;

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
    @Value("${kal.api.auth.admin.username}")
    private String userName;
    @Value("${kal.api.auth.admin.password}")
    private String userPassword;
    @Value("${kal.api.signing.url}")
    private String signingApiURL;
    @Inject
    private AuthClient authClient;

    public byte[] getEncodedSignedPdfDocument(byte[] encodedNonSignedPdf) {
        HttpEntity entity = new HttpEntity<>(encodedNonSignedPdf, authClient.getHeadersWithToken(userName, userPassword));

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> authResponse;
        try {
            authResponse = template.exchange(signingApiURL, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unknown error, unable to sign PDF document.", e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + authResponse.getBody() + "]");
        }

        return authResponse.getBody().getBytes();
    }
}
