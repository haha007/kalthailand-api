package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

import java.util.List;

/**
 * TODO Must migrate data from CommissionResult to CommissionCalculationSession
 * Should be replaced by new data structure.
 *
 * @deprecated replaced by {@link CommissionCalculationSession}.
 */
@Document(collection = "commissionResult")
@Deprecated
public class CommissionResult extends BaseEntity {
    @Deprecated
    private String rowId;
    @Deprecated
    //TODO should change to date
    private String commissionMonth;

    //TODO should be renamed by commissionCalculation
    @Deprecated
    private List<CommissionCalculation> policies;

    @Deprecated
    private Integer commissionPoliciesCount;

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getCommissionMonth() {
        return commissionMonth;
    }

    public void setCommissionMonth(String commissionMonth) {
        this.commissionMonth = commissionMonth;
    }

    public List<CommissionCalculation> getPolicies() {
        return policies;
    }

    public void setPolicies(List<CommissionCalculation> policies) {
        this.policies = policies;
    }

    public Integer getCommissionPoliciesCount() {
        return commissionPoliciesCount;
    }

    public void setCommissionPoliciesCount(Integer commissionPoliciesCount) {
        this.commissionPoliciesCount = commissionPoliciesCount;
    }

}
