package th.co.krungthaiaxa.api.elife.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.LogUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Instant startTime = LogUtil.logRequestStarting(httpServletRequest);

        // For swagger documentation, we should let any request to UI thing go through
        if (httpServletRequest.getMethod().equals("GET")) {
            if (httpServletRequest.getRequestURI().endsWith("/swagger-ui.html") ||
                    httpServletRequest.getRequestURI().endsWith("/v2/api-docs") ||
                    httpServletRequest.getRequestURI().endsWith("/swagger-resources") ||
                    httpServletRequest.getRequestURI().endsWith("/configuration/ui") ||
                    httpServletRequest.getRequestURI().endsWith("/configuration/security") ||
                    httpServletRequest.getRequestURI().endsWith("/project-info") ||
                    httpServletRequest.getRequestURI().contains("webjars/springfox-swagger-ui")) {
                chain.doFilter(httpServletRequest, response);
                return;
            }
        }

        // Very specific URLs that cannot send token when clicking on link to download file
        // TODO Should be removed
        if (httpServletRequest.getRequestURI().endsWith("/policies/extract/download") ||
                httpServletRequest.getRequestURI().contains("/RLS/deduction/download/") ||
                httpServletRequest.getRequestURI().contains("/session-quotes/all-products-counts/download") ||
                httpServletRequest.getRequestURI().contains("/commissions/calculation/download") ||
                (httpServletRequest.getRequestURI().contains("/policies/") &&
                        httpServletRequest.getRequestURI().contains("/document/") &&
                        httpServletRequest.getRequestURI().contains("/download"))) {
            chain.doFilter(httpServletRequest, response);
            return;
        }

        // Very specific URLs that cannot send token when clicking on link to download file
        // TODO Should be removed
        if (httpServletRequest.getRequestURI().contains("/adminwebsocket/policy-numbers/upload/")) {
            chain.doFilter(httpServletRequest, response);
            return;
        }

        // For everything else, we should check for token validity
        String authToken = httpServletRequest.getHeader(this.tokenHeader);

        // Token might be expired, always have to check for validity
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl + "/" + tokenRequiredRole);
        } catch (URISyntaxException e) {
            logger.error("Invalid URL [" + tokenValidationUrl + "/" + tokenRequiredRole + "]");
            sendErrorToResponse(ErrorCode.UI_UNAUTHORIZED.apply("Unable to check token validity"), (HttpServletResponse) response);
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
            sendErrorToResponse(ErrorCode.UI_UNAUTHORIZED.apply("Unable to check if current token gives access to API"), (HttpServletResponse) response);
            return;
        }

        if (validateRoleURIResponse.getStatusCode().value() != OK.value()) {
            sendErrorToResponse(ErrorCode.UI_UNAUTHORIZED.apply("Provided token doesn't give access to API"), (HttpServletResponse) response);
            return;
        }

        LogUtil.logRuntime(startTime, LogUtil.toStringRequestURL(httpServletRequest));
        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {

    }

    private void sendErrorToResponse(Error error, HttpServletResponse response) {
        byte[] content = JsonUtil.getJson(error);

        response.setHeader("Content-type", "application/json");
        response.setContentLength(content.length);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            logger.error("Unable to send error in response", e);
        }
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}