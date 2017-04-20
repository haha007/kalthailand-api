package th.co.krungthaiaxa.api.elife.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author tuong.le on 4/20/17.
 */
public class MocabStatus {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("policyNumber")
    private String policyNumber;

    @JsonProperty("messageCode")
    private String messageCode = "000"; //default value entity

    @JsonProperty("messageDetail")
    private String messageDetail;

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

    public String getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(final String messageDetail) {
        this.messageDetail = messageDetail;
    }
}
