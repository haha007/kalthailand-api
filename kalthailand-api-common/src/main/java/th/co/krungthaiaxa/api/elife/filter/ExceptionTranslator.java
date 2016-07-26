package th.co.krungthaiaxa.api.elife.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import th.co.krungthaiaxa.api.elife.exeption.BaseException;
import th.co.krungthaiaxa.api.elife.exeption.BeanValidationException;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.model.error.FieldError;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
public class ExceptionTranslator {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTranslator.class);

    private final BeanValidationExceptionTranslator beanValidationExceptionTranslator;

    @Inject
    public ExceptionTranslator(BeanValidationExceptionTranslator beanValidationExceptionTranslator) {this.beanValidationExceptionTranslator = beanValidationExceptionTranslator;}

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processUnknownInternalException(final Exception exception) {
        final Error result = ErrorCode.UNKNOWN_ERROR.apply(exception.getMessage());
        this.loggingMessage(result, exception);
        return result;
    }

    /**
     * In the beautiful life of client team, they don't want a lot of if-else statements.
     * So they don't want to use {@link HttpStatus#BAD_REQUEST} to handle this exception and {@link HttpStatus#}
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Error processValidationError(final MethodArgumentNotValidException exception) {
        final BindingResult bindingResult = exception.getBindingResult();
        List<FieldError> fieldErrorDTOs = beanValidationExceptionTranslator.toFieldErrorDTOs(bindingResult.getAllErrors());
        String fieldErrorsMessage = toPrettyMessageString(fieldErrorDTOs);
        final Error result = new Error(ErrorCode.ERROR_CODE_BEAN_VALIDATION, fieldErrorsMessage, exception.getMessage());
        result.setFieldErrors(fieldErrorDTOs);
        this.loggingMessage(result, exception);
        return result;
    }

    @ExceptionHandler(BeanValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processInternalException(final BeanValidationException exception) {
        final Error result = this.beanValidationExceptionTranslator.toErrorDTO(exception);
        this.loggingMessage(result, exception);
        return result;
    }

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processInternalException(final BaseException exception) {
        final Error result = new Error(exception.getErrorCode(), exception.getMessage(), exception.getMessage());
        this.loggingMessage(result, exception);
        return result;
    }

    private void loggingMessage(final Error error, final Exception ex) {
        final String errorMessage = String.format("Error code: %s\nUser message: %s.\nDeveloper message: %s", error.getCode(), error.getUserMessage(), error.getDeveloperMessage());
        LOGGER.error(errorMessage, ex);
    }

    private String toPrettyMessageString(List<FieldError> fieldErrors) {
        StringBuilder sb = new StringBuilder("The object has some invalid fields: ");
        String listFieldNames = fieldErrors.stream().map(
                fieldError1 -> fieldError1.getField()
        ).collect(Collectors.joining(", "));
        return sb.append(listFieldNames).toString();
    }
}
