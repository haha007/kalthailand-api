package th.co.krungthaiaxa.api.elife.commission.model;

/**
 * @author khoi.tran on 8/30/16.
 */
public class CommissionTarget {
    private CommissionTargetObject commissionTargetObject;
    private CommissionTargetType commissionTargetType;

    public CommissionTargetObject getCommissionTargetObject() {
        return commissionTargetObject;
    }

    public void setCommissionTargetObject(CommissionTargetObject commissionTargetObject) {
        this.commissionTargetObject = commissionTargetObject;
    }

    public CommissionTargetType getCommissionTargetType() {
        return commissionTargetType;
    }

    public void setCommissionTargetType(CommissionTargetType commissionTargetType) {
        this.commissionTargetType = commissionTargetType;
    }
}
