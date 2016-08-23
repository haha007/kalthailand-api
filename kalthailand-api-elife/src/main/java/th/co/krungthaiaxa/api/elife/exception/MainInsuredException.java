package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class MainInsuredException extends DefaultElifeException {

    public MainInsuredException(String message) {
        super(null, null, message);
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.ERROR_CODE_BEAN_VALIDATION;
    }
}
