package th.co.krungthaiaxa.api.common.exeption;

/**
 * This is the base exception of this project.
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = -8279292782665815881L;
    /**
     * At this moment (2016-04-19), the error code is number, but in future it can be string (e.g. "12.01.02").
     * That's why I use string here so that we can change errorCode easily.
     */
    private final String errorCode;

    private final String errorMessage;

    private final Object details;

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = errorCode;
        this.details = null;
    }

    public BaseException(String errorCode, String message, Object details) {
        super(message);
        this.errorMessage = message;
        this.errorCode = errorCode;
        this.details = details;
    }

    public BaseException(String errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
        this.errorMessage = throwable.getMessage();
        this.details = null;
    }

    public BaseException(String errorCode, String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.details = null;
    }

    public BaseException(String errorCode, String message, Object details, Throwable throwable) {
        super(message, throwable);
        this.errorCode = errorCode;
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
