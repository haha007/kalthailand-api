package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran
 */
public class BadArgumentException extends BaseException {
    private static final long serialVersionUID = -5046622369228818254L;
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_BAD_ARGUMENT;

    public BadArgumentException(String message) {
        super(ERROR_CODE, message);
    }

    public BadArgumentException(String message, Object details) {
        super(ERROR_CODE, message, details);
    }

    public BadArgumentException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
