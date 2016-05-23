package th.co.krungthaiaxa.api.elife.filter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class ClientSideRoleFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(ClientSideRoleFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
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

        if (httpServletRequest.getRequestURI().endsWith("/swagger-ui.html")) {
            // nothing to do for swagger ui
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

        Authentication authentication = (Authentication) ((HttpServletRequest) servletRequest).getUserPrincipal();
        List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, wrappedResponse);

        String modifiedResponse = modify(new String(wrappedResponse.getByteArray()), properties, roles);
        servletResponse.setContentLength(modifiedResponse.length());
        servletResponse.getOutputStream().write(modifiedResponse.getBytes());
    }

    @Override
    public void destroy() {
    }

    public static String modify(String html, Properties properties, List<String> roles) {
        if (properties == null) {
            return html;
        }

        if (roles == null || roles.size() == 0) {
            return html;
        }

        Document document = Jsoup.parse(html);
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            Element element = document.getElementById(propertyName);
            if (element != null) {
                String requiredRoles = properties.getProperty(propertyName);
                boolean hasRequiredRole = false;
                for (String role : roles) {
                    if (requiredRoles.contains(role)) {
                        hasRequiredRole = true;
                    }
                }

                if (!hasRequiredRole) {
                    element.remove();
                }
            }
        }

        return document.html();
    }

    private static class ByteArrayServletStream extends ServletOutputStream {
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

    private static class ByteArrayPrintWriter {
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
