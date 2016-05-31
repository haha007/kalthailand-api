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
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import java.io.IOException;

import static org.springframework.http.HttpMethod.POST;

@Service
public class BlackListClient {
    private final static Logger logger = LoggerFactory.getLogger(BlackListClient.class);
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${kal.api.blacklist.url}")
    private String blacklistApiURL;
    private RestTemplate template = new RestTemplate();

    public Boolean getCheckingBlackListed(String thaiId, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add(tokenHeader, token);

        HttpEntity<String> entity = new HttpEntity<>(thaiId, httpHeaders);

        ResponseEntity<String> authResponse;
        try {
            authResponse = template.exchange(blacklistApiURL, POST, entity, String.class);
        } catch (RestClientException e) {
            throw new ElifeException("Unknown error, unable to checking Blacklisted.", e);
        }

        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new ElifeException("Unable to create token. Response is [" + authResponse.getBody() + "]");
        }

        logger.info("Got checking Blacklisted from blacklist API");
        try {
            return JsonUtil.mapper.readValue(authResponse.getBody().getBytes(), Boolean.class);
        } catch (IOException e) {
            throw new ElifeException("Unable to convert response to boolean [" + authResponse.getBody() + "]", e);
        }
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
