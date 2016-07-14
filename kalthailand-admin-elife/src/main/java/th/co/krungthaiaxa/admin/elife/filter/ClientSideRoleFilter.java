package th.co.krungthaiaxa.admin.elife.filter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ClientSideRoleFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(ClientSideRoleFilter.class);

    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationUrl;

    private RestTemplate template = new RestTemplate();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
        SimpleDateFormat millisecondFormat = new SimpleDateFormat("SSS");
        DecimalFormat dcf = new DecimalFormat("#0.00");
        Date timeApiRequest = new Date();    
        
        if (!httpServletRequest.getRequestURI().endsWith(".htm") && !httpServletRequest.getRequestURI().endsWith(".html")) {
            // nothing to do on any request that is not to an html file
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!httpServletRequest.getMethod().equals("GET")) {
            // nothing to do on any request that is not a GET request
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        InputStream inputStream = this.getClass().getResourceAsStream("/static/uiRoleFilter.properties");
        if (inputStream == null) {
            logger.info("There is no file [/static/uiRoleFilter.properties]. Filter chain will resume without any alterations in request result.");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        Properties properties = new Properties();
        properties.load(inputStream);

        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, wrappedResponse);

        String modifiedResponse = modify(httpServletRequest, new String(wrappedResponse.getByteArray()), properties);
        servletResponse.setContentLength(modifiedResponse.length());
        
        Date timeApiResponse = new Date();
        long s1 = Integer.parseInt(secondFormat.format(timeApiRequest),10);
        long s2 = Integer.parseInt(secondFormat.format(timeApiResponse),10);
        long m1 = Integer.parseInt(millisecondFormat.format(timeApiRequest),10);
        long m2 = Integer.parseInt(millisecondFormat.format(timeApiResponse),10);
        long diffSecond = s2 - s1;
        long diffMillisecond = m2 - m1;
        double diffTotal = Double.parseDouble(diffSecond + "." + diffMillisecond);
        getAllOfRequestContent(httpServletRequest);
        logger.info("call to : " + httpServletRequest.getRequestURI() 
        + " request time is : " + sdf.format(timeApiRequest) 
        + " response time is : " + sdf.format(timeApiResponse)
        + " difference is : " + dcf.format(diffTotal) + " seconds. \n ---------------------------------------");
        
        servletResponse.getOutputStream().write(modifiedResponse.getBytes());
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

    private String modify(HttpServletRequest httpServletRequest, String html, Properties properties) {
        if (properties == null) {
            return html;
        }

        Document document = Jsoup.parse(html);
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            Element element = document.getElementById(propertyName);
            if (element != null) {
                String requiredRoles = properties.getProperty(propertyName);
                String[] requiredRoleList = requiredRoles.split(",");
                boolean hasRequiredRole = false;
                for (int i=0;i<requiredRoleList.length && !hasRequiredRole;i++) {
                    hasRequiredRole = validateTokenAgainstRole(httpServletRequest.getHeader(tokenHeader), requiredRoleList[i]);
                }

                if (!hasRequiredRole) {
                    element.remove();
                }
            }
        }

        return document.html();
    }

    private Boolean validateTokenAgainstRole(String token, String tokenRequiredRole) {
        URI validateRoleURI;
        try {
            validateRoleURI = new URI(tokenValidationUrl + "/" + tokenRequiredRole);
        } catch (URISyntaxException e) {
            logger.info("Unable to check token validity");
            return false;
        }

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(tokenHeader, token);
        UriComponentsBuilder validateRoleURIBuilder = UriComponentsBuilder.fromUri(validateRoleURI);
        ResponseEntity<String> validateRoleURIResponse;
        try {
            validateRoleURIResponse = template.exchange(validateRoleURIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);
        } catch (RestClientException e) {
            return false;
        }

        if (validateRoleURIResponse.getStatusCode().value() != OK.value()) {
            logger.info("Provided token doesn't give access to role [" + tokenRequiredRole + "]");
            return false;
        }

        return true;
    }

    private class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;
        WriteListener writeListener;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            this.writeListener = writeListener;
        }
    }

    private class ByteArrayPrintWriter {
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private PrintWriter pw = new PrintWriter(baos);
        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    public class CharResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayPrintWriter output;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new ByteArrayPrintWriter();
        }

        public byte[] getByteArray() {
            return output.toByteArray();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return output.getStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return output.getWriter();
        }

        public String toString() {
            return output.toString();
        }
    }
}
