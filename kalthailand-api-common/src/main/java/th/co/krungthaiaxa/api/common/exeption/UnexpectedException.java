package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * This is the base exception of this project.
 */
public class UnexpectedException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;
    /**
     * At this moment (2016-04-19), the error code is number, but in future it can be string (e.g. "12.01.02").
     * That's why I use string here so that we can change errorCode easily.
     */
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_UNKNOWN_ERROR;

    public UnexpectedException(String message) {
        super(ERROR_CODE, message);
    }

    public UnexpectedException(String message, Object details) {
        super(ERROR_CODE, message, details);
    }

    public UnexpectedException(Throwable throwable) {
        super(ERROR_CODE, throwable);
    }

    public UnexpectedException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

    public UnexpectedException(String message, Object details, Throwable throwable) {
        super(ERROR_CODE, message, details, throwable);
    }

}
