package th.co.krungthaiaxa.api.auth.model;

public class Error {
    private String code;
    private String userMessage;
    private String developerMessage;

    // Visibility has to be package so only ErrorCode can create Error instances
    Error(String code, String userMessage, String developerMessage) {
        this.code = code;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
    }

    public String getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

}
