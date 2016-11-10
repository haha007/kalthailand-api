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
import th.co.krungthaiaxa.admin.elife.log.RequestLogUtil;
import th.co.krungthaiaxa.admin.elife.service.AuthenticationClient;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Properties;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ClientSideRoleFilter implements Filter {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientSideRoleFilter.class);
    
    private final Properties uiRoleConfiguration = AuthenticationClient.UI_ROLE_CONFIG;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${kal.api.auth.token.validation.url}")
    private String tokenValidationUrl;

    private RestTemplate template = new RestTemplate();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private boolean isHtml(String uri) {
        if (uri == null) {
            return false;
        }
        String uriLowerCase = uri.toLowerCase();
        return uriLowerCase.endsWith(".htm") || uriLowerCase.endsWith(".html");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        Instant startTime = Instant.now();
        if (!isHtml(httpServletRequest.getRequestURI())) {
            // nothing to do on any request that is not to an html file
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!httpServletRequest.getMethod().equals("GET")) {
            // nothing to do on any request that is not a GET request
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, wrappedResponse);

        String modifiedResponse = modify(httpServletRequest, new String(wrappedResponse.getByteArray()), uiRoleConfiguration);
        servletResponse.setContentLength(modifiedResponse.length());

        RequestLogUtil.logRequestWithRunningTime(httpServletRequest, startTime);
        servletResponse.getOutputStream().write(modifiedResponse.getBytes());
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
                for (int i = 0; i < requiredRoleList.length && !hasRequiredRole; i++) {
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
            LOGGER.info("Unable to check token validity");
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
            LOGGER.info("Provided token doesn't give access to role [" + tokenRequiredRole + "]");
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
