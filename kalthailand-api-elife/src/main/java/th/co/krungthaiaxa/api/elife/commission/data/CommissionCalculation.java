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
    private double commissionPaymentTelrepresentative;
    private double commissionPaymentMkt;
    private double commissionPaymentCompany;

    private double firstYearPremiumFromRLS;
    private double firstYearCommissionFromRLS;

    public static class Agent {
        private CommissionTargetGroupType groupType;
        private String agentCode;
        private String agentStatus;
        private double commissionPayment;
    }
}
