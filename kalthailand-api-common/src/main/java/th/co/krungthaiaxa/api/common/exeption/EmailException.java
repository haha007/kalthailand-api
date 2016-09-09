package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class EmailException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_EMAIL_SENDER;

    public EmailException(String message) {
        super(ERROR_CODE, message);
    }

    public EmailException(String message, Object details) {
        super(ERROR_CODE, message);
    }

    public EmailException(Throwable throwable) {
        super(ERROR_CODE, throwable);
    }

    public EmailException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

    public EmailException(String message, Object details, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
