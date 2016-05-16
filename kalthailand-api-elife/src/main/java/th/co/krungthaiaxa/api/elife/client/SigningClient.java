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

import java.nio.charset.Charset;

import static org.springframework.http.HttpMethod.POST;

@Service
public class SigningClient {
    @Value("${kal.api.signing.url}")
    private String signingApiURL;
    @Inject
    private AuthClient authClient;

    public String getEncodedSignedPdfDocument(String encodedNonSignedPdf) {
        HttpEntity entity = new HttpEntity<>(encodedNonSignedPdf, authClient.getHeadersWithToken());

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> authResponse = null;
        try {
            authResponse = template.exchange(signingApiURL, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unknown error, unable to sign PDF document. Response is [" + authResponse.getBody() + "]", e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + authResponse.getBody() + "]");
        }

        return new String(authResponse.getBody().getBytes(), Charset.forName("UTF-8"));
    }
}
