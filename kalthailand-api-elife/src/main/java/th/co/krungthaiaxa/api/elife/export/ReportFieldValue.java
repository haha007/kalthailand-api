package th.co.krungthaiaxa.api.elife.export;

/**
 * @author khoi.tran on 12/26/16.
 */
public class ReportFieldValue {
    private String fieldDisplayName;
    private Object value;

    public String getFieldDisplayName() {
        return fieldDisplayName;
    }

    public void setFieldDisplayName(String fieldDisplayName) {
        this.fieldDisplayName = fieldDisplayName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
