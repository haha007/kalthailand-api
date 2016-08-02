package th.co.krungthaiaxa.admin.elife.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 8/2/16.
 */
public class RequestLogUtil {
    private final static Logger logger = LoggerFactory.getLogger(RequestLogUtil.class);

    public static void logRequestWithRunningTime(HttpServletRequest request, Instant startTime) {
        logRequestURI(request);
        logRequestTime(startTime);
    }

    public static void logRequestTime(Instant startTime) {
        Instant endTime = Instant.now();
        long runningMilliseconds = endTime.toEpochMilli() - startTime.toEpochMilli();
        double runningSeconds = ((double) runningMilliseconds / 1000);
        String msg = new StringBuilder()
                .append("\nStart time: ").append(startTime)
                .append("\nEnd time: ").append(endTime)
                .append(String.format("\nRunning time: %s ms (%.3f s)", runningMilliseconds, runningSeconds))
                .toString();
        logger.debug(msg);
    }

    public static void logRequestURI(HttpServletRequest request) {
        StringBuilder headersString = new StringBuilder();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            headersString.append("\n\t'").append(headerName).append("': ").append(request.getHeader(headerName));
        }

        String paramsString = request.getParameterMap().entrySet().stream().map(
                entry -> toStringParamEntry(entry)
        ).collect(Collectors.joining(", "));

        String msg = new StringBuilder()
                .append("\n|'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''|")
                .append("\nURL:").append(request.getRequestURL())
                .append("\nMethod:").append(request.getMethod())
                .append("\nHeaders:").append(headersString)
                .append("\nParams:").append(paramsString)
                .append("\n|'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''|")
                .toString();
        logger.debug(msg);
    }

    private static String toStringParamEntry(Map.Entry<String, String[]> param) {
        String paramValueString = null;
        String[] paramValues = param.getValue();
        if (paramValues != null) {
            if (paramValues.length == 1) {
                paramValueString = String.format("'%s'", paramValues[0]);
            } else {
                paramValueString = Arrays.asList(paramValues).stream().map(paramValue -> String.format("'%s'", paramValue)).collect(Collectors.joining(", ", "[", "]"));
            }
        }
        return String.format("{%s: %s}", param.getKey(), paramValueString);
    }
}
