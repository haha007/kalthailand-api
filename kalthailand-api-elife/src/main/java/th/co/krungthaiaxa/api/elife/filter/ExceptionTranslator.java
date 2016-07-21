package th.co.krungthaiaxa.api.elife.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
public class ExceptionTranslator {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTranslator.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error processUnknownInternalException(final Exception exception) {
        final Error result = ErrorCode.UNKNOWN_ERROR.apply(exception.getMessage());
        this.loggingMessage(result, exception);
        return result;
    }

    private void loggingMessage(final Error error, final Exception ex) {
        final String errorMessage = String.format("Error code: %s\nUser message: %s.\nDeveloper message: %s", error.getCode(), error.getUserMessage(), error.getDeveloperMessage());
        LOGGER.error(errorMessage, ex);
    }
}
