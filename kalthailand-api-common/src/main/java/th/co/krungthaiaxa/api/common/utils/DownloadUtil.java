package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * @author khoi.tran on 10/5/16.
 */
public class DownloadUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(DownloadUtil.class);

    public static void writeBytesToResponseWithFileNamePrefix(HttpServletResponse response, byte[] content, String fileNamePrefix) {
        String dateTime = DateTimeUtil.formatLocalDateTime(LocalDateTime.now(), "yyyyMMdd_HHmmss");
        String fileName = fileNamePrefix + dateTime + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(content.length);

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try {
            OutputStream outStream = response.getOutputStream();
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            LOGGER.error("Unable to download the quote total count excel file", e);
        }
    }
}
