package th.co.krungthaiaxa.api.elife.commission.util;

import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntityType;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroupType;

import java.util.List;

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

    public static CommissionPlan findCommissionPlan(String channelCode, String planCode, String customerCategory, List<CommissionPlan> commissionPlans) {
        CommissionPlan commissionPlan = new CommissionPlan();
        for (CommissionPlan icommissionPlan : commissionPlans) {
            if (icommissionPlan.getUnitCode().equals(channelCode) &&
                    icommissionPlan.getPlanCode().equals(planCode) &&
                    icommissionPlan.getCustomerCategory().name().equals(customerCategory)) {
                commissionPlan = icommissionPlan;
                break;
            }
        }
        return commissionPlan;
    }

    public static CommissionTargetGroup findCommissionTargetGroup(String type, List<CommissionTargetGroup> commissionTargetGroups) {
        CommissionTargetGroup result = null;
        for (CommissionTargetGroup commissionTargetGroup : commissionTargetGroups) {
            if (commissionTargetGroup.getTargetGroupType().name().equalsIgnoreCase(type)) {
                result = commissionTargetGroup;
            }
        }
        return result;
    }

    public static CommissionTargetEntity findTargetEntities(String entityType, CommissionTargetGroup commissionTargetGroup) {
        CommissionTargetEntity result = null;
        List<CommissionTargetEntity> targetEntities = commissionTargetGroup.getTargetEntities();
        for (CommissionTargetEntity targetEntity : targetEntities) {
            if (targetEntity.getTargetEntityType().name().equalsIgnoreCase(entityType)) {
                result = targetEntity;
                break;
            }
        }
        return result;
    }
}
