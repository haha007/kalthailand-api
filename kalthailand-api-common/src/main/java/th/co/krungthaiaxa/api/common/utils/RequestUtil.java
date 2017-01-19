package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.error.Error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 10/18/16.
 */
public class RequestUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);
    public static final String PATTERN_LOCAL_DATE = "yyyy-MM-dd";
    public static final String REQUEST_HEADER_ACCESS_TOKEN = "Authorization";

    public static String getAccessToken(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader(REQUEST_HEADER_ACCESS_TOKEN);
    }

    public static HttpSession createNewSession(HttpServletRequest httpServletRequest) {
        //Disabled the old session (which can be login with different account)
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return httpServletRequest.getSession(true);
    }

    public static void sendError(Error error, Integer status, HttpServletResponse response) {
        response.setContentType("text/x-json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(status);
        try {
            response.getWriter().write(new String(JsonUtil.getJson(error), "UTF-8"));
        } catch (IOException e) {
            throw new UnexpectedException("Unable to get valid error message. " + e.getMessage(), e);
        }
    }

    public static void sendErrorToResponse(Error error, HttpServletResponse response) {
        byte[] content = JsonUtil.getJson(error);

        response.setHeader("Content-type", "application/json");
        response.setContentLength(content.length);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            LOGGER.error("Unable to send error in response. " + e.getMessage(), e);
            throw new UnexpectedException("Unable to send error in response. " + e.getMessage(), e);
        }
    }

    public static String generateRequestParameters(Map<String, Object> parameters) {
        String paramsString = parameters.entrySet().stream().filter(entry -> entry.getValue() != null).map(entry -> generateRequestParameter(entry)).collect(Collectors.joining("&"));
        if (StringUtils.isNotBlank(paramsString)) {
            paramsString = "?" + paramsString;
        }
        return paramsString;
    }

    private static String generateRequestParameter(Map.Entry<String, Object> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return entry.getKey() + "=" + entry.getValue();
    }
}
