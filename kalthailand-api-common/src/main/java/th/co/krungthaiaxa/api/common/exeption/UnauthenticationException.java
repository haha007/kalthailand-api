package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/10/16.
 */
public class UnauthenticationException extends BaseException {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_AUTHENTICATION;

    public UnauthenticationException(String message) {
        super(ERROR_CODE, message);

    }

    public UnauthenticationException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
