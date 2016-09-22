package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * This is the base exception of this project.
 */
public class CommissionCalculationSessionException extends BaseException {

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_EMAIL_SENDER;

    public CommissionCalculationSessionException(String message) {
        super(ERROR_CODE, message);
    }

    public CommissionCalculationSessionException(Throwable throwable) {
        super(ERROR_CODE, throwable);
    }
}
