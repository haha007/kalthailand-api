package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author tuong.le on 3/6/17.
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

    //Mapping message code and message detail from MOCAB vendor
    public static String mappingMessageDetail(final String messageCode) {
        final String messageDetail;
        switch (messageCode) {
            case "200":
                messageDetail = "OK";
                break;
            case "400":
                messageDetail = "Bad Request";
                break;
            case "600":
                messageDetail = "Required Validation Error";
                break;
            case "601":
                messageDetail = "Product unknown";
                break;
            case "602":
                messageDetail = "Policy Existed";
                break;
            case "603":
                messageDetail = "Customer telephone no. is incorrect";
                break;
            case "604":
                messageDetail = "Policy status unknown";
                break;
            case "605":
                messageDetail = "Document Type unknown";
                break;
            case "500":
                messageDetail = "Policy Number is blank";
                break;
            case "501":
                messageDetail = "Policy Number not found";
                break;
            case "502":
                messageDetail = "Policy Status unknown";
                break;
            case "503":
                messageDetail = "KeySign for policyStatus VALIDATED only";
                break;
            case "504":
                messageDetail = "Policy status is blank";
                break;
            case "505":
                messageDetail = "Policy status updated";
                break;
            case "506":
                messageDetail = "Update PolicyStatus must have keySign";
                break;
            default:
                messageDetail = "Message unknown";
        }
        return messageDetail;
    }
}
