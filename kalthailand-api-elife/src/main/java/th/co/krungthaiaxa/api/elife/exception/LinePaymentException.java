package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran
 */
public class LinePaymentException extends BaseException {
    private static final long serialVersionUID = -5046622369228818254L;
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_LINE_PAYMENT;

    public LinePaymentException(String message) {
        super(ERROR_CODE, message);
    }

    public LinePaymentException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
