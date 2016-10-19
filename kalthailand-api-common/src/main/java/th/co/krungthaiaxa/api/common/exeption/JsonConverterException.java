package th.co.krungthaiaxa.api.common.exeption;

import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

public class JsonConverterException extends BaseException {
    private static final long serialVersionUID = -8279292782665815881L;

    private final static String ERROR_CODE = ErrorCode.ERROR_CODE_JSON_CONVERTER;

    public JsonConverterException(String message, Object details, Throwable throwable) {
        super(ERROR_CODE, message, details, throwable);
    }

}
