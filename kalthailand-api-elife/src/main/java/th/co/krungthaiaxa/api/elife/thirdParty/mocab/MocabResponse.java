package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by tuong.le on 3/6/17.
 */
public class MocabResponse implements Serializable {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("policyNumber")
    private String policyNumber;

    @JsonProperty("messageCode")
    private String messageCode;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(final String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(final String messageCode) {
        this.messageCode = messageCode;
    }
}
