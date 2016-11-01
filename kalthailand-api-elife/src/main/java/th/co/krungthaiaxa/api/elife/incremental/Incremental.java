package th.co.krungthaiaxa.api.elife.incremental;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author khoi.tran on 10/28/16.
 */
@Document(collection = "incremental")
public class Incremental {
    @Id
    private String key;
    private long value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
