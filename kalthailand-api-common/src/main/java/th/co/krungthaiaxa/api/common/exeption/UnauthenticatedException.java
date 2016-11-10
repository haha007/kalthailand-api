package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/10/16.
 */
public class UnauthenticatedException extends BaseException {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_AUTHENTICATION;

    public UnauthenticatedException(String message) {
        super(ERROR_CODE, message);

    }

    public UnauthenticatedException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
