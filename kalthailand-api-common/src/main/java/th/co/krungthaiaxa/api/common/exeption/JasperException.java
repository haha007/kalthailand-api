package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class JasperException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_JASPER;

    public JasperException(String message) {
        super(ERROR_CODE, message);
    }

    public JasperException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

}
