package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class FileIOException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_FILE_IO;

    public FileIOException(String message) {
        super(ERROR_CODE, message);
    }

    public FileIOException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

}
