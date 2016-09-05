package th.co.krungthaiaxa.api.elife.commission.data;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import java.time.Instant;
import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commission")
public class CommissionPlan {
    @Id
    private ObjectId id;
    @Indexed
    private String unitCode;
    /**
     * PlanCode is the combination productType and packageName (e.g. {@link ProductType#PRODUCT_IFINE}_{@link th.co.krungthaiaxa.api.elife.products.ProductIFinePackage#IFINE1}.
     * Most of the time it's equals to {@link ProductType#getName()}.
     */
    @Indexed
    private String planCode;
    @Indexed
    private CustomerCategory customerCategory;
    private List<CommissionTargetGroup> targetGroups;

    private Instant createdDateTime;
    private Instant updatedDateTime;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

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

    public Instant getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Instant createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Instant getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(Instant updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}
