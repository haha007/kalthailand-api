package th.co.krungthaiaxa.api.elife.model.error;

public class Error {
    private String code;
    private String userMessage;
    private String developerMessage;

    // Used by Jackson
    public Error() {
    }

    // Visibility has to be package so only ErrorCode can create Error instances
    Error(String code, String userMessage, String developerMessage) {
        this.code = code;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
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
