package th.co.krungthaiaxa.api.elife.incremental;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * @author khoi.tran on 10/28/16.
 */
@Document(collection = "incremental")
public class Incremental {
    @Id
    private String key;
    @Indexed
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
