package th.co.krungthaiaxa.api.signing.model;

public class Error {
    private String timestamp;
    private String status;
    private String path;
    private String code;
    private String userMessage;
    private String developerMessage;

    public Error() {
    }

    Error(String code, String userMessage, String developerMessage) {
        this.code = code;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }
}
