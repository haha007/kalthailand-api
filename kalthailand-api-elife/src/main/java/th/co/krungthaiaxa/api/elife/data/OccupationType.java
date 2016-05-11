package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class OccupationType {
    @Id
    private String id;
    @Indexed(unique = true)
    private Integer occId;
    private String occTextTh;
    private String occTextEn;
    private Boolean occRisk;

    public String getId() {
        return id;
    }

    public Integer getOccId() {
        return occId;
    }

    public String getOccTextTh() {
        return occTextTh;
    }

    public String getOccTextEn() {
        return occTextEn;
    }

    public Boolean getOccRisk() {
        return occRisk;
    }
}
