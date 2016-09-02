package th.co.krungthaiaxa.api.elife.commission.data;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
public class CommissionTargetGroup {
    private CommissionTargetGroupType targetGroupType;
    private List<CommissionTargetEntity> targetEntities;

    public CommissionTargetGroupType getTargetGroupType() {
        return targetGroupType;
    }

    public void setTargetGroupType(CommissionTargetGroupType targetGroupType) {
        this.targetGroupType = targetGroupType;
    }

    public List<CommissionTargetEntity> getTargetEntities() {
        return targetEntities;
    }

    public void setTargetEntities(List<CommissionTargetEntity> targetEntities) {
        this.targetEntities = targetEntities;
    }
}
