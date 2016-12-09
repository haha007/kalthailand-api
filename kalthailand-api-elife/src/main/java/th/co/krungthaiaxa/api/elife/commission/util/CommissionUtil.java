package th.co.krungthaiaxa.api.elife.commission.util;

import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntityType;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroupType;

/**
 * @author khoi.tran on 12/9/16.
 */
public class CommissionUtil {

    public static CommissionTargetGroup getTargetGroup(CommissionPlan commissionPlan, CommissionTargetGroupType commissionTargetGroupType) {
        if (commissionPlan.getTargetGroups() == null) {
            return null;
        }
        for (CommissionTargetGroup commissionTargetGroup : commissionPlan.getTargetGroups()) {
            if (commissionTargetGroupType.equals(commissionTargetGroup.getTargetGroupType())) {
                return commissionTargetGroup;
            }
        }
        return null;
    }

    public static CommissionTargetEntity getTargetEntity(CommissionTargetGroup commissionTargetGroup, CommissionTargetEntityType commissionTargetEntityType) {
        if (commissionTargetGroup.getTargetEntities() == null) {
            return null;
        }
        for (CommissionTargetEntity commissionTargetEntity : commissionTargetGroup.getTargetEntities()) {
            if (commissionTargetEntityType.equals(commissionTargetEntity.getTargetEntityType())) {
                return commissionTargetEntity;
            }
        }
        return null;
    }
}
