package th.co.krungthaiaxa.api.elife.commission.data.cdb;

/**
 * @author khoi.tran on 9/14/16.
 */
public class CDBPolicyCommissionEntity {

    private String policyNumber;
    private String policyStatus;
    private String planCode;
    private String paymentCode;
    private String agentCode;
    private Double firstYearPremium;
    private Double firstYearCommission;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public Double getFirstYearPremium() {
        return firstYearPremium;
    }

    public void setFirstYearPremium(Double firstYearPremium) {
        this.firstYearPremium = firstYearPremium;
    }

    public Double getFirstYearCommission() {
        return firstYearCommission;
    }

    public void setFirstYearCommission(Double firstYearCommission) {
        this.firstYearCommission = firstYearCommission;
    }
}
