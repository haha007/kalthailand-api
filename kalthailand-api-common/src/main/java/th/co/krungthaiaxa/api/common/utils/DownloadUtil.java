package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * @author khoi.tran on 10/5/16.
 */
public class DownloadUtil {
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final static Logger LOGGER = LoggerFactory.getLogger(DownloadUtil.class);

    public static void writeBytesToResponse(HttpServletResponse response, byte[] content, String fileName, DownloadType downloadType) {
        String contentType;
        if (downloadType == DownloadType.XLSX) {
            contentType = CONTENT_TYPE_XLSX;
        } else if (downloadType == DownloadType.PDF) {
            contentType = CONTENT_TYPE_PDF;
        } else {
            throw new UnexpectedException("Not support this DownloadType yet: " + downloadType);
        }
        writeBytesToResponse(response, content, fileName, contentType);
    }

    public static void writeBytesToResponseWithDateRollFileName(HttpServletResponse response, byte[] content, String fileNamePrefix) {
        String dateTime = DateTimeUtil.formatLocalDateTime(LocalDateTime.now(), "yyyyMMdd_HHmmss");
        String fileName = fileNamePrefix + "_" + dateTime + ".xlsx";
        writeBytesToResponse(response, content, fileName, CONTENT_TYPE_XLSX);
    }

    public static void writeBytesToResponse(HttpServletResponse response, byte[] content, String fileName, String contentType) {
        response.setContentType(contentType);
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

    public enum DownloadType {
        PDF, XLSX
    }
}
