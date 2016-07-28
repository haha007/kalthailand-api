package th.co.krungthaiaxa.api.elife.exeption;

import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;

public class EmailException extends RuntimeException {
    private static final long serialVersionUID = -8279292782665815881L;

    private final String errorCode = ErrorCode.ERROR_CODE_EMAIL_SENDER;

    private final String errorMessage;

    private final Object details;

    public EmailException(String message) {
        super(message);
        this.errorMessage = message;
        this.details = null;
    }

    public EmailException(String message, Object details) {
        super(message);
        this.errorMessage = message;
        this.details = details;
    }

    public EmailException(Throwable throwable) {
        super(throwable);
        this.errorMessage = throwable.getMessage();
        this.details = null;
    }

    public EmailException(String message, Throwable throwable) {
        super(message, throwable);
        this.errorMessage = message;
        this.details = null;
    }

    public EmailException(String message, Object details, Throwable throwable) {
        super(message, throwable);
        this.errorMessage = message;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getDetails() {
        return this.details;
    }
}
