package th.co.krungthaiaxa.api.elife.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.model.authentication.RoleConstants;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class KalApiTokenFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(KalApiTokenFilter.class);
    public static final String URI_REGEXP_POLICIES_MAIN_INSURED_PERSON = "^.*/policies/[0-9a-zA-Z\\-]+/main-insured/person.*";

    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationUrl;
    @Value("${kal.api.auth.required.role}")
    private String tokenRequiredRole;

    @Autowired
    private AuthorizationClient securityService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Instant startTime = LogUtil.logRequestStarting(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
        // For swagger documentation, we should let any request to UI thing go through
        List<String> publicURIsForGETMethod = Arrays.asList(
                "/health"
                , "/swagger-ui.html"
                , "/v2/api-docs"
                , "/swagger-resources"
                , "/configuration/ui"
                , "/configuration/security"
                , "/project-info"
                , "webjars/springfox-swagger-ui"
        );
        if (httpServletRequest.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            for (String publicURI : publicURIsForGETMethod) {
                if (requestURI.contains(publicURI)) {
                    chain.doFilter(httpServletRequest, response);
                    return;
                }
            }
        }

        // Very specific URLs that cannot send token when clicking on link to download file
        List<String> regexPublicURIs = Arrays.asList(
                ".*/autoconfig"
                , ".*/actuator"
//                , ".*/trace"
//                , ".*/beans$"
//                , ".*/configprops"
//                , ".*/dump"
//                , ".*/env"
                , ".*/loggers"
                , ".*/metrics"
//                , ".*/mappings"
                , ".*/policies/extract/download.*"
                , ".*/adminwebsocket/policy-numbers/upload/.*"
                , ".*/RLS/deduction/download/.*"
                , ".*/session-quotes/all-products-counts/download.*"
                , ".*/quotes/all-products/download.*"
                , ".*/commissions/calculation-session/download/.*"
                , ".*/policies/.*/pdf.*"
                , ".*/policies/.*/download.*"
                , ".*/documents/.*/download.*"
        );
        // TODO Should be removed
        for (String publicURI : regexPublicURIs) {
            Pattern pattern = Pattern.compile(publicURI);
            if (pattern.matcher(requestURI).matches()) {
                chain.doFilter(httpServletRequest, response);
                return;
            }
        }

        // For everything else, we should check for token validity
        String authToken = RequestUtil.getAccessToken(httpServletRequest);
        try {
            boolean authorized = securityService.checkPermission(authToken, tokenRequiredRole);
            if (authorized) {
                authorized = securityService.checkPermission(httpServletRequest, URI_REGEXP_POLICIES_MAIN_INSURED_PERSON, HttpMethod.POST, RoleConstants.UI_ELIFE_ADMIN);
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

}