package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class FileNotFoundException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_FILE_NOT_FOUND;

    public FileNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
    public FileNotFoundException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

}
