package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionCalculation")
public class CommissionCalculation extends BaseEntity {
    private String policyNumber;
    private String policyStatus;
    private double commissionPercentage;
    private double overridePercentage;

    private Agent agent1;
    private Agent agent2;
    private double commissionPaymentDistribute1;
    private double commissionPaymentDistribute2;
    private double commissionPaymentTelRepresentative;
    private double commissionPaymentMkt;
    private double commissionPaymentCompany;

    private double firstYearPremiumFromRLS;
    private double firstYearCommissionFromRLS;

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

    public double getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(double commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public double getOverridePercentage() {
        return overridePercentage;
    }

    public void setOverridePercentage(double overridePercentage) {
        this.overridePercentage = overridePercentage;
    }

    public Agent getAgent1() {
        return agent1;
    }

    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    public Agent getAgent2() {
        return agent2;
    }

    public void setAgent2(Agent agent2) {
        this.agent2 = agent2;
    }

    public double getCommissionPaymentDistribute1() {
        return commissionPaymentDistribute1;
    }

    public void setCommissionPaymentDistribute1(double commissionPaymentDistribute1) {
        this.commissionPaymentDistribute1 = commissionPaymentDistribute1;
    }

    public double getCommissionPaymentDistribute2() {
        return commissionPaymentDistribute2;
    }

    public void setCommissionPaymentDistribute2(double commissionPaymentDistribute2) {
        this.commissionPaymentDistribute2 = commissionPaymentDistribute2;
    }

    public double getCommissionPaymentTelRepresentative() {
        return commissionPaymentTelRepresentative;
    }

    public void setCommissionPaymentTelRepresentative(double commissionPaymentTelRepresentative) {
        this.commissionPaymentTelRepresentative = commissionPaymentTelRepresentative;
    }

    public double getCommissionPaymentMkt() {
        return commissionPaymentMkt;
    }

    public void setCommissionPaymentMkt(double commissionPaymentMkt) {
        this.commissionPaymentMkt = commissionPaymentMkt;
    }

    public double getCommissionPaymentCompany() {
        return commissionPaymentCompany;
    }

    public void setCommissionPaymentCompany(double commissionPaymentCompany) {
        this.commissionPaymentCompany = commissionPaymentCompany;
    }

    public double getFirstYearPremiumFromRLS() {
        return firstYearPremiumFromRLS;
    }

    public void setFirstYearPremiumFromRLS(double firstYearPremiumFromRLS) {
        this.firstYearPremiumFromRLS = firstYearPremiumFromRLS;
    }

    public double getFirstYearCommissionFromRLS() {
        return firstYearCommissionFromRLS;
    }

    public void setFirstYearCommissionFromRLS(double firstYearCommissionFromRLS) {
        this.firstYearCommissionFromRLS = firstYearCommissionFromRLS;
    }

    public static class Agent {
        private CommissionTargetGroupType groupType;
        private String agentCode;
        private String agentStatus;
        private double commissionPayment;

        public CommissionTargetGroupType getGroupType() {
            return groupType;
        }

        public void setGroupType(CommissionTargetGroupType groupType) {
            this.groupType = groupType;
        }

        public String getAgentCode() {
            return agentCode;
        }

        public void setAgentCode(String agentCode) {
            this.agentCode = agentCode;
        }

        public String getAgentStatus() {
            return agentStatus;
        }

        public void setAgentStatus(String agentStatus) {
            this.agentStatus = agentStatus;
        }

        public double getCommissionPayment() {
            return commissionPayment;
        }

        public void setCommissionPayment(double commissionPayment) {
            this.commissionPayment = commissionPayment;
        }
    }
}
