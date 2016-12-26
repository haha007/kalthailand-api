package th.co.krungthaiaxa.api.elife.export;

/**
 * @author khoi.tran on 12/26/16.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Validate that the annotated string is not {@code null} or empty.
 * The difference to {@code NotEmpty} is that trailing whitespaces are getting ignored.
 *
 * @author Hardy Ferentschik
 */
@Documented
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportField {

    /**
     * The key to be used to store the field inside the document.
     *
     * @return
     */
    String value() default "";
}
