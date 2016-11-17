package th.co.krungthaiaxa.api.elife.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author khoi.tran on 11/16/16.
 */
@Service
public class AuthorizationClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthorizationClient.class);
    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationContexth;
    private RestTemplate template = new RestTemplate();

    public boolean checkPermission(HttpServletRequest httpServletRequest, String authorizeResourceUri, HttpMethod authorizeHttpMethod, String requiredRole) {
        String requestURI = httpServletRequest.getRequestURI();
        if (!requestURI.matches(authorizeResourceUri) || httpServletRequest.getMethod() == null || !httpServletRequest.getMethod().equalsIgnoreCase(authorizeHttpMethod.name())) {
            return true;
        }
        String accessToken = RequestUtil.getAccessToken(httpServletRequest);
        return checkPermission(accessToken, requiredRole);
    }

    public boolean checkPermission(String accessToken, String requiredRole) {

        // Token might be expired, always have to check for validity
        String tokenValidationUrl = tokenValidationContexth + "/" + requiredRole;
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl);
        } catch (URISyntaxException e) {
            throw new UnexpectedException("Invalid URL [" + tokenValidationUrl + "]: " + e.getMessage(), e);
        }

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(RequestUtil.REQUEST_HEADER_ACCESS_TOKEN, accessToken);
        UriComponentsBuilder validateRoleURIBuilder = UriComponentsBuilder.fromUri(validateRoleURI);
        ResponseEntity<String> validateRoleURIResponse = template.exchange(validateRoleURIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);
        return validateRoleURIResponse.getStatusCode() == OK;
    }
}
