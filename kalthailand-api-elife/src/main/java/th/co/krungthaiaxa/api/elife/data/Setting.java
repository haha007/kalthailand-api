package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.PolicySetting;

@Document
public class Setting {
    @Id
    private String id;

    private PolicySetting policySetting;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PolicySetting getPolicySetting() {
        return policySetting;
    }

    public void setPolicySetting(PolicySetting policySetting) {
        this.policySetting = policySetting;
    }
}
