package th.co.krungthaiaxa.api.common.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.exeption.BeanValidationException;
import th.co.krungthaiaxa.api.common.exeption.BeanValidationExceptionIfc;
import th.co.krungthaiaxa.api.common.exeption.UnauthenticationException;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.model.error.FieldError;
import th.co.krungthaiaxa.api.common.utils.ExceptionUtil;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import javax.inject.Inject;
import java.sql.SQLException;
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

    @ExceptionHandler({ UnauthenticationException.class })
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Error processUnauthenticationException(final UnauthenticationException exception) {
        return processInternalException(exception);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class, HttpRequestMethodNotSupportedException.class, ServletRequestBindingException.class, TypeMismatchException.class, HttpMessageNotReadableException.class })
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error processBadRequestException(final Exception exception) {
        final Error result = ErrorCode.BAD_REQUEST.apply(exception.getMessage());
        this.loggingMessage(result, exception);
        return result;
    }

    /**
     * In the beautiful life of client team, they don't want a lot of if-else statements.
     * So they don't want to use {@link HttpStatus#BAD_REQUEST} to handle this exception and {@link HttpStatus#}
     * This method is similar to {@link #processValidationError(BeanValidationException)}.
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
    public Error processValidationError(final BeanValidationException exception) {
        final Error result = this.beanValidationExceptionTranslator.toErrorDTO(exception);
        this.loggingMessage(result, exception);
        return result;
    }

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processInternalException(final BaseException exception) {
        final Error result = new Error(exception.getErrorCode(), exception.getMessage(), exception.getMessage());
        result.setDetails(exception.getDetails());
        result.setDetailsType(exception.getDetailsType());
        this.loggingMessage(result, exception);
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processUnknownInternalException(final Exception exception) {
        Error result;
        if (exception instanceof BeanValidationExceptionIfc) {
            result = this.beanValidationExceptionTranslator.toErrorDTO((BeanValidationExceptionIfc) exception);
        } else if (exception instanceof SQLException || exception instanceof DataAccessException) {
            result = new Error(ErrorCode.ERROR_CODE_UNKNOWN_ERROR, "SQL Query error. " + ExceptionUtil.getDataExceptionRoot(exception), "SQL Query error " + exception.getMessage());
        } else {
            String message = exception.getMessage();
            if (exception instanceof NullPointerException) {
                message += ". " + ExceptionUtil.getNullPointerExceptionRoot(exception);
            }
            result = ErrorCode.UNKNOWN_ERROR.apply(message);
        }
        this.loggingMessage(result, exception);
        return result;
    }

    private String toPrettyMessageString(List<FieldError> fieldErrors) {
        StringBuilder sb = new StringBuilder("The object has some invalid fields: ");
        String listFieldNames = fieldErrors.stream().map(
                fieldError1 -> fieldError1.getField()
        ).collect(Collectors.joining(", "));
        return sb.append(listFieldNames).toString();
    }

    private void loggingMessage(final Error error, final Exception ex) {
        String errorMessage = String.format("Error code: %s%nUser message: %s.%nDeveloper message: %s", error.getCode(), error.getUserMessage(), error.getDeveloperMessage());
        String errorDetailString;
        try {
            errorDetailString = ObjectMapperUtil.toJson(JsonUtil.mapper, error);
        } catch (Exception toStringEx) {
            errorDetailString = ObjectMapperUtil.toStringMultiLine(error);
            LOGGER.warn("Error when convert Error object to String:" + error + "%n" + toStringEx.getMessage(), toStringEx);
        }
        errorMessage += "%n" + errorDetailString;
        if (error.getCode().equals(ErrorCode.ERROR_CODE_BEAN_VALIDATION)) {
            //Don't need to show full detail stacktrace message in this case because it usually mistake from user's input.
            //Should ignore the Sonar warning here.
            LOGGER.error(errorMessage);
        } else {
            LOGGER.error(errorMessage, ex);
        }
    }
}
