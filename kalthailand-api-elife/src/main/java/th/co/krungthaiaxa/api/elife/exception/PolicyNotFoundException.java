package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran
 */
public class PolicyNotFoundException extends BaseException {
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_POLICY_NOT_EXIST;

    public PolicyNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public PolicyNotFoundException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
