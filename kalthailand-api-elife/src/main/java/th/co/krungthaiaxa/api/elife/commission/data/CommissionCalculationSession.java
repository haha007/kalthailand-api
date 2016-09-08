package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionCalculationSession")
public class CommissionCalculationSession extends BaseEntity {

    private List<CommissionPlan> commissionPlans;
    private List<CommissionCalculation> commissionCalculations;
}
