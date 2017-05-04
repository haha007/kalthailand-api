package th.co.krungthaiaxa.api.elife.tmc;


/**
 * TMC Sending PDF Response Remark
 * @deprecated Do not use TMC service anymore, change to Mocab service instead
 */
@Deprecated
public class TMCSendingPDFResponseRemark {
    private String policyNo;
    private String message;

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
