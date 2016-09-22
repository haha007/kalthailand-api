package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionCalculationSession")
public class CommissionCalculationSession extends BaseEntity {

    @DBRef
    private List<CommissionPlan> commissionPlans;
    @DBRef
    private List<CommissionCalculation> commissionCalculations;

    public List<CommissionPlan> getCommissionPlans() {
        return commissionPlans;
    }

    public void setCommissionPlans(List<CommissionPlan> commissionPlans) {
        this.commissionPlans = commissionPlans;
    }

    public List<CommissionCalculation> getCommissionCalculations() {
        return commissionCalculations;
    }

    public void setCommissionCalculations(List<CommissionCalculation> commissionCalculations) {
        this.commissionCalculations = commissionCalculations;
    }
}
