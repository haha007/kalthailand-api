package th.co.krungthaiaxa.api.elife.data;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.validator.ElementEmail;
import th.co.krungthaiaxa.api.common.validator.ElementNotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Document(collection = "policyNumberSetting")
public class PolicyNumberSetting {
    @Id
    private String id;
    /**
     * When the real policy numbers reach more than this percentage, it will notify to end user.
     */
    @NotNull
    @Min(1)
    @Max(100)
    private Integer triggerPercent;

    /**
     * The notification emails.
     */
    @NotNull
    @NotEmpty
    @ElementEmail
    @ElementNotBlank
    private List<String> emailList;

    /**
     * The time trigger setting (in seconds).
     */
    @NotNull
    private Long timeTrigger;

    public Long getTimeTrigger() {
        return timeTrigger;
    }

    public void setTimeTrigger(Long timeTrigger) {
        this.timeTrigger = timeTrigger;
    }

    public Integer getTriggerPercent() {
        return triggerPercent;
    }

    public void setTriggerPercent(Integer triggerPercent) {
        this.triggerPercent = triggerPercent;
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

    @Override
    public String toString() {
        return "PolicyNumberSetting [id=" + id + ", triggerPercent=" + triggerPercent + ", emailList=" + emailList
                + ", timeTrigger=" + timeTrigger + "]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
