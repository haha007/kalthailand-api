package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 10/3/16.
 */
public class QuoteNotExistException extends BaseException {
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_QUOTE_NOT_EXIST;

    public QuoteNotExistException(String message) {
        super(ERROR_CODE, message);
    }

    public QuoteNotExistException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}