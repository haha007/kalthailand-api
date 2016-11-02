package th.co.krungthaiaxa.api.common.utils;

import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.FieldError;

import java.util.List;
import java.util.Optional;

/**
 * @author khoi.tran on 11/2/16.
 */
public class ErrorUtil {
    public static Optional<FieldError> findFieldError(Error error, String fieldName) {
        List<FieldError> fieldErrors = error.getFieldErrors();
        if (fieldErrors == null) {
            return Optional.empty();
        }
        return fieldErrors.stream().filter(fieldError -> fieldName.equals(fieldError.getField())).findAny();
    }
}
