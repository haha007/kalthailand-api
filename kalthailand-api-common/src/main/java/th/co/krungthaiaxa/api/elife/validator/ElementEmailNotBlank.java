package th.co.krungthaiaxa.api.elife.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author khoi.tran on 7/25/16.
 */
@Documented
@Constraint(validatedBy = ElementEmailNotBlankValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementEmailNotBlank {
    String message() default "The list is not validated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean ignoreCase() default false;
}