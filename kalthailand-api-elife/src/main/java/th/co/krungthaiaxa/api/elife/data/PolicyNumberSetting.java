package th.co.krungthaiaxa.api.elife.data;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Document
public class PolicyNumberSetting {
    @Id
    private String id;
    /**
     * When the real policy numbers reach more than this percentage, it will notify to end user.
     */
    @NotNull
    @Min(1)
    @Max(100)
    private Integer percent;

    @NotNull
    @NotEmpty
    private List<String> emailList;

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

    @Override
    public String toString() {
        return "PolicyNumberSetting [percent=" + percent + ", emailList=" + emailList + "]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
