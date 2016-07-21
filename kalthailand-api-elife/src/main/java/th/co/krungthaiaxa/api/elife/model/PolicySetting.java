package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Setting for policy")
public class PolicySetting {
    private Integer quota;

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }
}
