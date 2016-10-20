package th.co.krungthaiaxa.api.common.model;

/**
 * @author khoi.tran on 10/20/16.
 */
public class DownloadFile {
    private String fileName;
    private String content;
    private String mimeType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
