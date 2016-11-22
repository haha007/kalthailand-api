package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/22/16.
 */
public class LineNotificationException extends BaseException {
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_LINE_NOTIFICATION;

    public LineNotificationException(String message) {
        super(ERROR_CODE, message);
    }

    public LineNotificationException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
