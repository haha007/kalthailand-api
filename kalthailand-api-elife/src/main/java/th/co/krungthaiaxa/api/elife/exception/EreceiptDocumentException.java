package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran
 */
public class EreceiptDocumentException extends BaseException {
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_DOCUMENT_ERECEIPT;

    public EreceiptDocumentException(String message) {
        super(ERROR_CODE, message);
    }

    public EreceiptDocumentException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }
}
