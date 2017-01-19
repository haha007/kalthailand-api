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

    public static void writeBytesToResponseWithFullFileName(HttpServletResponse response, byte[] content, String fullFileName, DownloadType downloadType) {
        String mimeType = getContentType(downloadType);
        writeBytesToResponseWithFullFileName(response, content, fullFileName, mimeType);
    }

    public static String getContentType(DownloadType downloadType) {
        String contentType;
        if (downloadType == DownloadType.XLSX) {
            contentType = CONTENT_TYPE_XLSX;
        } else if (downloadType == DownloadType.PDF) {
            contentType = CONTENT_TYPE_PDF;
        } else {
            throw new UnexpectedException("Not support this DownloadType yet: " + downloadType);
        }
        return contentType;
    }

    public static void writeExcelBytesToResponseWithDateRollFileName(HttpServletResponse response, byte[] content, String fileNamePrefix) {
        String dateTime = DateTimeUtil.formatLocalDateTime(LocalDateTime.now(), "yyyyMMdd_HHmmss");
        String fileName = fileNamePrefix + "_" + dateTime + ".xlsx";
        writeBytesToResponseWithFullFileName(response, content, fileName, CONTENT_TYPE_XLSX);
    }

    /**
     * @param response
     * @param content
     * @param fileName doesn't have extension. The extension will be decided by mimeType.
     * @param mimeType
     */
    public static void writeBytesToResponse(HttpServletResponse response, byte[] content, String fileName, String mimeType) {
        String fullFileName = fileName;
        String fileExtension = MimeTypeUtil.getFileExtensionFromMimeType(mimeType);
        if (fileExtension != null) {
            fullFileName += "." + fileExtension;
        }
        writeBytesToResponseWithFullFileName(response, content, fullFileName, mimeType);
    }

    /**
     * @param response
     * @param content
     * @param fullFileName file name include extension.
     * @param mimeType
     */
    public static void writeBytesToResponseWithFullFileName(HttpServletResponse response, byte[] content, String fullFileName, String mimeType) {
        response.setContentType(mimeType);
        response.setContentLength(content.length);

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fullFileName);
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
