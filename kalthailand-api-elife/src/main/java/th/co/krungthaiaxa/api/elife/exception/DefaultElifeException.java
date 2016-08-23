package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BeanValidationExceptionIfc;

import javax.validation.ConstraintViolation;
import java.util.Set;

public abstract class DefaultElifeException extends ElifeException implements BeanValidationExceptionIfc {

    private final Object errorTarget;
    private final Set<ConstraintViolation<Object>> violations;

    public DefaultElifeException(Object errorTarget, Set<ConstraintViolation<Object>> violations, String message) {
        super(message);
        this.errorTarget = errorTarget;
        this.violations = violations;
    }

    @Override
    public Object getErrorTarget() {
        return errorTarget;
    }

    @Override
    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

}
