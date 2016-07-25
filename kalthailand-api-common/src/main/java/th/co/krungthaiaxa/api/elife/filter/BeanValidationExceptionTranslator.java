package th.co.krungthaiaxa.api.elife.filter;

import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;
import th.co.krungthaiaxa.api.elife.exeption.BeanValidationException;
import th.co.krungthaiaxa.api.elife.model.error.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by khoi.tran on 4/13/16.
 */
@Component
public class BeanValidationExceptionTranslator {

    /**
     * @param violation violation rule.
     * @return return field name of violation rule (also find in super class of error object).
     */
    private String getFieldName(ConstraintViolation<Object> violation) {
        Path propertyPath = violation.getPropertyPath();
        return propertyPath != null ? propertyPath.toString() : null;
    }

    private String getTargetName(ConstraintViolation<Object> violation) {
        Object targetObject = violation.getLeafBean();
        return targetObject != null ? targetObject.getClass().getSimpleName() : null;
    }

    public FieldError toFieldErrorDTO(ConstraintViolation<Object> violation) {
        ConstraintDescriptor<?> constraintDescriptor = violation.getConstraintDescriptor();
        Class<? extends Annotation> annotationType = constraintDescriptor.getAnnotation().annotationType();
        String fieldValidatorCode;
        if (annotationType.equals(ScriptAssert.class)) {
            ConstraintDescriptor<ScriptAssert> scriptAssertConstraintDescriptor = (ConstraintDescriptor<ScriptAssert>) constraintDescriptor;
            fieldValidatorCode = scriptAssertConstraintDescriptor.getAnnotation().message();
        } else {
            fieldValidatorCode = annotationType.getSimpleName();
        }
        String targetName = getTargetName(violation);
        String fieldName = getFieldName(violation);
        String msg = String.format("%s: %s", fieldName, violation.getMessage());
        return new FieldError(fieldValidatorCode, targetName, fieldName, msg);
    }

    public FieldError toFieldErrorDTO(ObjectError objectError) {
        if (objectError instanceof org.springframework.validation.FieldError) {
            org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
            return new FieldError(fieldError.getCode(), fieldError.getObjectName(), fieldError.getField(), fieldError.getDefaultMessage());
        } else {
            String errorCode = objectError.getCode();
            if (errorCode.equals(ScriptAssert.class.getSimpleName())) {
                errorCode = objectError.getDefaultMessage();
            }
            return new FieldError(errorCode, objectError.getObjectName(), null, objectError.getDefaultMessage());
        }
    }

    public th.co.krungthaiaxa.api.elife.model.error.Error toErrorDTO(BeanValidationException ex) {
        th.co.krungthaiaxa.api.elife.model.error.Error result = new th.co.krungthaiaxa.api.elife.model.error.Error(ex.getErrorCode(), ex.getMessage(), ex.getMessage());
        List<FieldError> fieldErrors = toFieldErrorDTOs(ex.getViolations());
        result.setFieldErrors(fieldErrors);
        return result;
    }

    public List<FieldError> toFieldErrorDTOs(Set<ConstraintViolation<Object>> violations) {
        if (violations == null) {
            return null;
        }
        return violations.stream().map(violation -> toFieldErrorDTO(violation)).collect(Collectors.toList());
    }

    public List<FieldError> toFieldErrorDTOs(List<ObjectError> objectErrors) {
        if (objectErrors == null) {
            return null;
        }
        return objectErrors.stream().map(objectError -> toFieldErrorDTO(objectError)).collect(Collectors.toList());
    }

}
