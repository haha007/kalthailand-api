package th.co.krungthaiaxa.api.elife.commission.data;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 *         This is the result of commission calculation
 */
@Document(collection = "commissionCalculationSession")
public class CommissionCalculationSession extends BaseEntity {
    private List<CommissionPlan> commissionPlans;
    private List<CommissionCalculation> commissionCalculations;

    @ApiModelProperty(value = "This commission session is calculated on the payments which proccessed before the commissionDate (within one month)")
    @NotNull
    private Instant commissionDate;

    private String resultCode;
    private String resultMessage;

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

    public Instant getCommissionDate() {
        return commissionDate;
    }

    public void setCommissionDate(Instant commissionDate) {
        this.commissionDate = commissionDate;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
