package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionPlan")
public class CommissionPlan extends BaseEntity {
    @Indexed
    /**
     * This can be any string. For example 40002, 20001...
     */
    private String unitCode;
    /**
     * PlanCode is the combination productType and packageName (e.g. {@link ProductType#PRODUCT_IFINE}_{@link th.co.krungthaiaxa.api.elife.products.ProductIFinePackage#IFINE1}.
     * Most of the time it's equals to {@link ProductType#getLogicName()}.
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
