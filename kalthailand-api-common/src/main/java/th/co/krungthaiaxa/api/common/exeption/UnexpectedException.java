package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * This is the base exception of this project.
 */
public class UnexpectedException extends RuntimeException {
    private static final long serialVersionUID = -8279292782665815881L;
    /**
     * At this moment (2016-04-19), the error code is number, but in future it can be string (e.g. "12.01.02").
     * That's why I use string here so that we can change errorCode easily.
     */
    private final String errorCode = ErrorCode.ERROR_CODE_UNKNOWN_ERROR;

    private final String errorMessage;

    private final Object details;

    public UnexpectedException(String message) {
        super(message);
        this.errorMessage = message;
        this.details = null;
    }

    public UnexpectedException(String message, Object details) {
        super(message);
        this.errorMessage = message;
        this.details = details;
    }

    public UnexpectedException(Throwable throwable) {
        super(throwable);
        this.errorMessage = throwable.getMessage();
        this.details = null;
    }

    public UnexpectedException(String message, Throwable throwable) {
        super(message, throwable);
        this.errorMessage = message;
        this.details = null;
    }

    public UnexpectedException(String message, Object details, Throwable throwable) {
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
