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
        FileType fileType = getContentType(downloadType);
        writeBytesToResponse(response, content, fileName, fileType.getMimeType());
    }

    private static FileType getContentType(DownloadType downloadType) {
        FileType contentType;
        if (downloadType == DownloadType.XLSX) {
            contentType = new FileType(".xlsx", CONTENT_TYPE_XLSX);
        } else if (downloadType == DownloadType.PDF) {
            contentType = new FileType(".pdf", CONTENT_TYPE_PDF);
        } else {
            throw new UnexpectedException("Not support this DownloadType yet: " + downloadType);
        }
        return contentType;
    }

    private static class FileType {
        private String fileExtension;
        private String mimeType;

        private FileType(String fileExtension, String mimeType) {
            this.fileExtension = fileExtension;
            this.mimeType = mimeType;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public void setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }

    public static void writeExcelBytesToResponseWithDateRollFileName(HttpServletResponse response, byte[] content, String fileNamePrefix) {
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
