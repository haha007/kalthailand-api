package th.co.krungthaiaxa.api.elife.model.error;

import java.io.Serializable;

public class FieldError implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;

    private final String objectName;

    private final String field;

    private final String message;

    public FieldError(String code, String objectName, String field, String message) {
        this.code = code;
        this.objectName = objectName;
        this.field = field;
        this.message = message;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public String getField() {
        return this.field;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }
}
