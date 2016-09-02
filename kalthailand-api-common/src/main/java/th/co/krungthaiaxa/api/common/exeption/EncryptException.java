package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class EncryptException extends RuntimeException {
    private static final long serialVersionUID = -8279292782665815881L;

    private final String errorCode = ErrorCode.ERROR_CODE_ENCRYPT;

    private final String errorMessage;

    private final Object details;

    public EncryptException(String message) {
        super(message);
        this.errorMessage = message;
        this.details = null;
    }

    public EncryptException(String message, Object details) {
        super(message);
        this.errorMessage = message;
        this.details = details;
    }

    public EncryptException(Throwable throwable) {
        super(throwable);
        this.errorMessage = throwable.getMessage();
        this.details = null;
    }

    public EncryptException(String message, Throwable throwable) {
        super(message, throwable);
        this.errorMessage = message;
        this.details = null;
    }

    public EncryptException(String message, Object details, Throwable throwable) {
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
