package th.co.krungthaiaxa.ebiz.api.model.error;

public class Error {
    private String code;
    private String userMessage;
    private String developerMessage;

    // Used by Jackson
    public Error() {
    }

    public Error(String code, String userMessage, String developerMessage) {
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
