package th.co.krungthaiaxa.api.elife.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.elife.security.AuthorizationClient;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class KalApiTokenFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(KalApiTokenFilter.class);
    public static final String URI_REGEXP_POLICIES_MAIN_INSURED_PERSON = "^.+/policies/[0-9a-zA-Z\\-]+/main-insured/person";

    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationUrl;
    @Value("${kal.api.auth.required.role}")
    private String tokenRequiredRole;

    @Autowired
    private AuthorizationClient securityService;

    private RestTemplate template = new RestTemplate();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Instant startTime = LogUtil.logRequestStarting(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
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
                httpServletRequest.getRequestURI().contains("/quotes/all-products/download") ||
                httpServletRequest.getRequestURI().contains("/commissions/calculation/download/") ||
                (httpServletRequest.getRequestURI().contains("/policies/") && httpServletRequest.getRequestURI().contains("/pdf")) ||
                (httpServletRequest.getRequestURI().contains("/policies/") && httpServletRequest.getRequestURI().contains("/download"))
                ) {
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
        String authToken = RequestUtil.getAccessToken(httpServletRequest);
        try {
            boolean authorized = securityService.checkPermission(authToken, tokenRequiredRole);
            if (authorized) {
                authorized = securityService.checkPermission(httpServletRequest, URI_REGEXP_POLICIES_MAIN_INSURED_PERSON, HttpMethod.POST, "UI_ELIFE_ADMIN");
            }
            if (authorized) {
                LogUtil.logRuntime(startTime, LogUtil.toStringRequestURL(httpServletRequest));
                chain.doFilter(request, response);
            } else {
                LogUtil.logRuntime(startTime, LogUtil.toStringRequestURL(httpServletRequest));
                RequestUtil.sendErrorToResponse(ErrorCode.UI_UNAUTHORIZED.apply("Doesn't have permission to access API "), (HttpServletResponse) response);
            }
        } catch (Exception ex) {
            LogUtil.logRuntime(startTime, LogUtil.toStringRequestURL(httpServletRequest));
            RequestUtil.sendErrorToResponse(ErrorCode.UI_UNAUTHORIZED.apply("Cannot access API: " + ex.getMessage()), (HttpServletResponse) response);
        }
    }

    @Override
    public void destroy() {

    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}