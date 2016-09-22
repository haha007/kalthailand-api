package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran
 */
public class PaymentHasNewerCompletedException extends BaseException {
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_PAYMENT_HAS_NEWER_COMPLETED;

    public PaymentHasNewerCompletedException(Object detail, String message) {
        super(ERROR_CODE, message, detail);
    }
}
