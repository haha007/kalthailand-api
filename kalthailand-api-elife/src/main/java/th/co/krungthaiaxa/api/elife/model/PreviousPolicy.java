package th.co.krungthaiaxa.api.elife.model;

import java.io.Serializable;

/**
 * @author khoi.tran on 12/8/16.
 */
public class PreviousPolicy implements Serializable {
    private String policyNumber;
    private String agentCode1;
    private String agentCode2;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getAgentCode1() {
        return agentCode1;
    }

    public void setAgentCode1(String agentCode1) {
        this.agentCode1 = agentCode1;
    }

    public String getAgentCode2() {
        return agentCode2;
    }

    public void setAgentCode2(String agentCode2) {
        this.agentCode2 = agentCode2;
    }
}
