package th.co.krungthaiaxa.api.signing.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.signing.model.ErrorCode;
import th.co.krungthaiaxa.api.signing.model.Error;
import th.co.krungthaiaxa.api.signing.utils.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

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
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
        SimpleDateFormat millisecondFormat = new SimpleDateFormat("SSS");
        DecimalFormat dcf = new DecimalFormat("#0.00");
        Date timeApiRequest = new Date();     
        
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
            sendErrorToResponse(ErrorCode.UNAUTHORIZED.apply("No token provided"), (HttpServletResponse) response);
            return;
        }

        // Token might be expired, always have to check for validity
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl + "/" + tokenRequiredRole);
        } catch (URISyntaxException e) {
            logger.error("Invalid URL [" + tokenValidationUrl + "/" + tokenRequiredRole + "]");
            sendErrorToResponse(ErrorCode.UNAUTHORIZED.apply("Unable to check token validity"), (HttpServletResponse) response);
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
            sendErrorToResponse(ErrorCode.UNAUTHORIZED.apply("Unable to check if current token gives access to API"), (HttpServletResponse) response);
            return;
        }

        if (validateRoleURIResponse.getStatusCode().value() != OK.value()) {
            sendErrorToResponse(ErrorCode.UNAUTHORIZED.apply("Provided token doesn't give access to API"), (HttpServletResponse) response);
            return;
        }
        
        Date timeApiResponse = new Date();
        long s1 = Integer.parseInt(secondFormat.format(timeApiRequest),10);
        long s2 = Integer.parseInt(secondFormat.format(timeApiResponse),10);
        long m1 = Integer.parseInt(millisecondFormat.format(timeApiRequest),10);
        long m2 = Integer.parseInt(millisecondFormat.format(timeApiResponse),10);
        long diffSecond = s2 - s1;
        long diffMillisecond = m2 - m1;
        double diffTotal = Double.parseDouble(diffSecond + "." + diffMillisecond);
        getAllOfRequestContent(httpRequest);
        logger.info("call to : " + httpRequest.getRequestURI() 
        + " request time is : " + sdf.format(timeApiRequest) 
        + " response time is : " + sdf.format(timeApiResponse)
        + " difference is : " + dcf.format(diffTotal) + " seconds. \n ---------------------------------------");

        chain.doFilter(request, response);
    }
    
    private void getAllOfRequestContent(HttpServletRequest request){
    	logger.info("|'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''|");
    	//method
    	String method = request.getMethod();
    	logger.info("Method is : "+method);
    	//header
    	Enumeration headerNames = request.getHeaderNames();
    	while(headerNames.hasMoreElements()) {
    	  String headerName = (String)headerNames.nextElement();
    	  logger.info("Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
    	}
    	//body
    	Enumeration params = request.getParameterNames(); 
    	while(params.hasMoreElements()){
    	 String paramName = (String)params.nextElement();
    	 logger.info("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
    	}
    	logger.info("|................................................................|");
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