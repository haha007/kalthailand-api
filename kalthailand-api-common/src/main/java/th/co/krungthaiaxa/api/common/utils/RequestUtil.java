package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import th.co.krungthaiaxa.api.common.model.error.Error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author khoi.tran on 10/18/16.
 */
public class RequestUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);
    public static final String PATTERN_LOCAL_DATE = "yyyy-MM-dd";
    private static final String REQUEST_HEADER_ACCESS_TOKEN = "Authorization";

    public static String getAccessToken(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader(REQUEST_HEADER_ACCESS_TOKEN);
    }

    public static void sendError(Error error, Integer status, HttpServletResponse response) {
        response.setContentType("text/x-json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(status);
        try {
            response.getWriter().write(new String(JsonUtil.getJson(error), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to get valid error message.", e);
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
            LOGGER.error("Unable to send error in response", e);
        }
    }
}
