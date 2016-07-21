package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Setting for policy")
public class PolicySetting {
    private Integer quotaInMonth;

    public Integer getQuotaInMonth() {
        return quotaInMonth;
    }

    public void setQuotaInMonth(Integer quotaInMonth) {
        this.quotaInMonth = quotaInMonth;
    }
}
