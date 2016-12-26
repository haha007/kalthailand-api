package th.co.krungthaiaxa.api.elife.export;

import java.lang.reflect.Field;

/**
 * @author khoi.tran on 12/26/16.
 */
public class ReportFieldDescription {
    private Field field;
    private String fieldDisplayName;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getFieldDisplayName() {
        return fieldDisplayName;
    }

    public void setFieldDisplayName(String fieldDisplayName) {
        this.fieldDisplayName = fieldDisplayName;
    }
}
