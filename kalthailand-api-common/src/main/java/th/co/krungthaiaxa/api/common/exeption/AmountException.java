package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class AmountException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_AMOUNT;

    public AmountException(String message) {
        super(ERROR_CODE, message);
    }


    public AmountException(String message, Throwable throwable) {
        super(ERROR_CODE, message, throwable);
    }

}
