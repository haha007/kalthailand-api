package th.co.krungthaiaxa.api.blacklist.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Component
public class KalApiTokenFilter implements Filter {
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationUrl;
    @Value("${kal.api.auth.required.role}")
    private String tokenRequiredRole;

    private RestTemplate template = new RestTemplate();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // Swagger requests shoud always go through
        if (httpRequest.getRequestURI().endsWith("/v2/api-docs") ||
                httpRequest.getRequestURI().endsWith("/configuration/ui") ||
                httpRequest.getRequestURI().endsWith("/swagger-resources") ||
                httpRequest.getRequestURI().endsWith("/configuration/security") ||
                httpRequest.getRequestURI().endsWith("/swagger-ui.html") ||
                httpRequest.getRequestURI().endsWith("/images/favicon-16x16.png") ||
                httpRequest.getRequestURI().endsWith("/images/favicon-32x32.png") ||
                httpRequest.getRequestURI().contains("/webjars/springfox-swagger-ui/")) {
            chain.doFilter(request, response);
            return;
        }

        // Requests to error API should always go through
        if (httpRequest.getRequestURI().endsWith("/error")) {
            chain.doFilter(request, response);
            return;
        }

        String authToken = httpRequest.getHeader(this.tokenHeader);

        if (StringUtils.isEmpty(authToken)) {
            throw new IOException("No token provided");
        }

        // Token might be expired, always have to check for validity
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl + "/" + tokenRequiredRole);
        } catch (URISyntaxException e) {
            throw new IOException("Unable to check token validity");
        }

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(tokenHeader, authToken);
        UriComponentsBuilder validateRoleURIBuilder = UriComponentsBuilder.fromUri(validateRoleURI);
        ResponseEntity<String> validateRoleURIResponse;
        try {
            validateRoleURIResponse = template.exchange(validateRoleURIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);
        } catch (RestClientException e) {
            throw new IOException("Unable to check if current token gives access to API", e);
        }

        if (validateRoleURIResponse.getStatusCode().value() != OK.value()) {
            throw new IOException("Provided token doesn't give access to API");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}