package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/10/16.
 */
public class UnauthorizationException extends BaseException {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_AUTHORIZATION;

    public UnauthorizationException(String message) {
        super(ERROR_CODE, message);

    }

    public UnauthorizationException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
