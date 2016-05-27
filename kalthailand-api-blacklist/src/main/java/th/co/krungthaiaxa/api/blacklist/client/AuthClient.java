package th.co.krungthaiaxa.api.blacklist.client;

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
import org.springframework.web.util.UriComponentsBuilder;

import th.co.krungthaiaxa.api.blacklist.exception.ElifeException;
import th.co.krungthaiaxa.api.blacklist.utils.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.HttpMethod.POST;

@Service
public class AuthClient {
    private final static Logger logger = LoggerFactory.getLogger(AuthClient.class);
    @Value("${kal.api.auth.token.create.url}")
    private String createTokenUrl;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    private RestTemplate template = new RestTemplate();

    public HttpHeaders getHeadersWithToken(String userName, String password) {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI;
        try {
            authURI = new URI(createTokenUrl);
        } catch (URISyntaxException e) {
            throw new ElifeException("Unable to get Auth API URL", e);
        }

        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI);
        RequestForToken requestForToken = new RequestForToken();
        requestForToken.setUserName(userName);
        requestForToken.setPassword(password);

        ResponseEntity<String> authResponse;
        try {
            authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(requestForToken, authURIHeaders), String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unable to connect to auth API to create token using URL [" + createTokenUrl + "]", e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token; Response is [" + authResponse.getBody() + "]");
        }

        Token token;
        try {
            token = JsonUtil.mapper.readValue(authResponse.getBody(), Token.class);
        } catch (IOException e) {
            throw new ElifeException("Unable to read token content", e);
        }

        HttpHeaders result = new HttpHeaders();
        result.add("Content-Type", "application/json");
        result.add(tokenHeader, token.getToken());

        logger.info("Got token from auth api");
        return result;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
