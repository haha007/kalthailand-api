package th.co.krungthaiaxa.api.elife.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.HttpMethod.POST;

@Service
public class AuthClient {
    @Value("${kal.api.auth.token.create.url}")
    private String createTokenUrl;
    @Value("${kal.api.auth.username}")
    private String userName;
    @Value("${kal.api.auth.password}")
    private String userPassword;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    public HttpHeaders getHeadersWithToken() {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI;
        try {
            authURI = new URI(createTokenUrl);
        } catch (URISyntaxException e) {
            throw new ElifeException("Unable to connect to Auth API to get token");
        }

        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName)
                .queryParam("password", userPassword);

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(authURIHeaders), String.class);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token; Response is [" + authResponse.getBody() + "]");
        }

        HttpHeaders result = new HttpHeaders();
        result.add("Content-Type", "application/json");
        result.add(tokenHeader, authResponse.getBody());

        return result;
    }

}
