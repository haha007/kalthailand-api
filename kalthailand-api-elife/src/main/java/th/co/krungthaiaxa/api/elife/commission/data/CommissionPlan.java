package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionPlan")
public class CommissionPlan extends BaseEntity {
    @Indexed
    /**
     * This can be any string. For example 40002, 20002...
     * But usually the number.
     */
    private String unitCode;
    /**
     * PlanCode is defined by CDB system which mapping to product package, for example:
     * 5W10L for iProtect
     * iFINE1 for iFine, package iFine1
     */
    @Indexed
    private String planCode;
    @Indexed
    private CustomerCategory customerCategory;
    private List<CommissionTargetGroup> targetGroups;

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public CustomerCategory getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(CustomerCategory customerCategory) {
        this.customerCategory = customerCategory;
    }

    public List<CommissionTargetGroup> getTargetGroups() {
        return targetGroups;
    }

    public void setTargetGroups(List<CommissionTargetGroup> targetGroups) {
        this.targetGroups = targetGroups;
    }
}
