package th.co.krungthaiaxa.api.common.log;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 8/2/16.
 */
public class LogHttpRequestUtil {
    public static void logFinishing(Instant startTime, HttpServletRequest request) {
        LogUtil.logFinishing(startTime, toLogMessage(request));
    }

    public static Instant logStarting(HttpServletRequest request) {
        return LogUtil.logStarting(toLogMessage(request));
    }

    private static String toLogMessage(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder("'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''");
        sb.append("\n").append(toStringRequestURL(request));
        sb.append("\n\tHeaders: ").append(toStringRequestHeaders(request));
        sb.append("\n\tParams: ").append(toStringRequestParams(request));
        return sb.toString();
    }

    private static String toStringRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        List<String> headerStrings = new ArrayList<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            String headerString = String.format("{%s:%s}", headerName, headerValue);
            headerStrings.add(headerString);
        }
        return headerStrings.stream().collect(Collectors.joining(","));
    }

    private static String toStringRequestParams(HttpServletRequest request) {
        return request.getParameterMap().toString();
    }

    private static String toStringRequestURL(HttpServletRequest request) {
        return String.format("%s %s", request.getMethod(), request.getRequestURL());
    }

}
