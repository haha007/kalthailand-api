package th.co.krungthaiaxa.api.blacklist.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Component
public class KalApiTokenFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(KalApiTokenFilter.class);
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
        Instant startTime = LogUtil.logRequestStarting(httpRequest);

        // Swagger requests shoud always go through
        if (httpRequest.getRequestURI().endsWith("/v2/api-docs") ||
                httpRequest.getRequestURI().endsWith("/configuration/ui") ||
                httpRequest.getRequestURI().endsWith("/swagger-resources") ||
                httpRequest.getRequestURI().endsWith("/configuration/security") ||
                httpRequest.getRequestURI().endsWith("/swagger-ui.html") ||
                httpRequest.getRequestURI().endsWith("/images/favicon-16x16.png") ||
                httpRequest.getRequestURI().endsWith("/images/favicon-32x32.png") ||
                httpRequest.getRequestURI().endsWith("/project-info") ||
                httpRequest.getRequestURI().contains("/webjars/springfox-swagger-ui/")) {
            chain.doFilter(request, response);
            return;
        }

        // Requests to error API should always go through
        if (httpRequest.getRequestURI().endsWith("/error")) {
            chain.doFilter(request, response);
            return;
        }

        // Very specific URLs that cannot send token when clicking on link to download file
        // TODO Should be removed
        if (httpRequest.getRequestURI().contains("/adminwebsocket/blacklist/upload/")) {
            chain.doFilter(httpRequest, response);
            return;
        }

        // Token might be expired, always have to check for validity
        String authToken = httpRequest.getHeader(this.tokenHeader);
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl + "/" + tokenRequiredRole);
        } catch (URISyntaxException e) {
            logger.error("Invalid URL [" + tokenValidationUrl + "/" + tokenRequiredRole + "]");
            RequestUtil.sendErrorToResponse(ErrorCode.UNAUTHORIZED_BLACKLIST.apply("Unable to check token validity"), (HttpServletResponse) response);
            return;
        }

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(tokenHeader, authToken);
        UriComponentsBuilder validateRoleURIBuilder = UriComponentsBuilder.fromUri(validateRoleURI);
        ResponseEntity<String> validateRoleURIResponse;
        try {
            validateRoleURIResponse = template.exchange(validateRoleURIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);
        } catch (RestClientException e) {
            RequestUtil.sendErrorToResponse(ErrorCode.UNAUTHORIZED_BLACKLIST.apply("Unable to check if current token gives access to API"), (HttpServletResponse) response);
            return;
        }

        if (validateRoleURIResponse.getStatusCode().value() != OK.value()) {
            RequestUtil.sendErrorToResponse(ErrorCode.UNAUTHORIZED_BLACKLIST.apply("Provided token doesn't give access to API"), (HttpServletResponse) response);
            return;
        }

        LogUtil.logRuntime(startTime, LogUtil.toStringRequestURL(httpRequest));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}