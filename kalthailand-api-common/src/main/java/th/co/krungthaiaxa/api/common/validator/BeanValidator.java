package th.co.krungthaiaxa.api.common.validator;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.exeption.BeanValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author khoi.tran
 */
@Component
public class BeanValidator {

    private Validator validator;

    public BeanValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * This method will validate base on annotations in the bean (e.g. @NotNull, @NotEmpty, @Email...).
     *
     * @param target the object which will be validated. If the target is null, we don't need to validate it, there's no Exception will be thrown.
     *               This behaviour is the same as javax.validation.Validator (don't validate with null properties).
     * @throws BeanValidationException if there's any wrong field value, it will throw a {@link BeanValidationException} object.
     */
    public void validate(Object target) {
        if (target != null) {
            Set<ConstraintViolation<Object>> violations = validator.validate(target);
            if (!violations.isEmpty()) {
                throw new BeanValidationException("Error in BeanValidator", target, violations);
            }
        }
    }
}
