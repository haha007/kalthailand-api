package th.co.krungthaiaxa.api.elife.line.v2.model;

/**
 * @author tuong.le on 10/24/17.
 */
public class PolicyIdLineUserIdMap {
    private String policyId;
    private String userLineId;

    public PolicyIdLineUserIdMap(String policyId, String userLineId) {
        this.policyId = policyId;
        this.userLineId = userLineId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getUserLineId() {
        return userLineId;
    }

    public void setUserLineId(String userLineId) {
        this.userLineId = userLineId;
    }
}
