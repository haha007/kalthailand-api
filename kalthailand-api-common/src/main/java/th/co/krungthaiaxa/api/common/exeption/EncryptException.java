package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class EncryptException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_ENCRYPT;

    public EncryptException(String message, Object details) {
        super(ERROR_CODE, message, details);
    }

    public EncryptException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

}
