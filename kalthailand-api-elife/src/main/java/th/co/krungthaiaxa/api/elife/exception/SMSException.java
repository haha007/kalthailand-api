package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;

/**
 * @author khoi.tran on 10/17/16.
 */
public class SMSException extends BaseException {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_SMS;

    public SMSException(String message) {
        super(ERROR_CODE, message);
    }

    public SMSException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

    public SMSException(String message, SMSResponse details) {
        super(ERROR_CODE, message, details);
    }
}
