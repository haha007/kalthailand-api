package th.co.krungthaiaxa.api.common.exeption;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface BeanValidationExceptionIfc {
    Object getErrorTarget();

    Set<ConstraintViolation<Object>> getViolations();

    String getErrorCode();

    String getMessage();
}
