package th.co.krungthaiaxa.api.elife.commission.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionPlanRepository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionPlanService {
    private final CommissionPlanRepository commissionPlanRepository;

    @Inject
    public CommissionPlanService(CommissionPlanRepository commissionPlanRepository) {this.commissionPlanRepository = commissionPlanRepository;}

    public List<CommissionPlan> findAll() {
        return commissionPlanRepository.findAll();
    }

    public List<CommissionPlan> saveCommissionPlans(List<CommissionPlan> commissionPlans) {
        commissionPlanRepository.deleteAll();
        for (CommissionPlan commissionPlan : commissionPlans) {
            preSaveCommissionPlan(commissionPlan);
        }
        return commissionPlanRepository.save(commissionPlans);
    }

    private void preSaveCommissionPlan(CommissionPlan commissionPlan) {
        //TODO validate percentage, unitcode, product name...
        LocalDateTime processingDateTime = LocalDateTime.now();
        if (commissionPlan.getCreatedDateTime() == null) {
            commissionPlan.setCreatedDateTime(processingDateTime);
        }
        commissionPlan.setUpdatedDateTime(processingDateTime);
    }
}
