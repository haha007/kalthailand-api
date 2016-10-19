package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

import javax.validation.ConstraintViolation;
import java.util.Collections;
import java.util.Set;

/**
 * @author khoi.tran
 */
public class BeanValidationException extends BaseException implements BeanValidationExceptionIfc {
    private static final long serialVersionUID = -5046622369228818254L;
    public static final String ERROR_CODE = ErrorCode.ERROR_CODE_BEAN_VALIDATION;

    private final transient Object errorTarget;

    private final transient Set<ConstraintViolation<Object>> violations;

    /**
     * @param message     general error message.
     * @param errorTarget the object which contains invalid properties.
     * @param violations  the validation result.
     */
    public BeanValidationException(final String message, final Object errorTarget, final Set<ConstraintViolation<Object>> violations) {
        super(ERROR_CODE, message);
        this.errorTarget = errorTarget;
        this.violations = Collections.unmodifiableSet(violations);
    }

    public Object getErrorTarget() {
        return this.errorTarget;
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return this.violations;
    }
}
