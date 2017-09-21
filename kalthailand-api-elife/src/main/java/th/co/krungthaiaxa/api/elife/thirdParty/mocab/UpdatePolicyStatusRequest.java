package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

/**
 * @author tuong.le on 3/6/17.
 */
public class UpdatePolicyStatusRequest {
    private String keySign;
    private PolicyStatus policyStatus;

    public String getKeySign() {
        return keySign;
    }

    public void setKeySign(String keySign) {
        this.keySign = keySign;
    }

    public PolicyStatus getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
    }
}
