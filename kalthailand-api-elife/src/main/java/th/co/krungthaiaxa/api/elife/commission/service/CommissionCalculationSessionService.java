package th.co.krungthaiaxa.api.elife.commission.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.CommissionCalculationSessionException;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.repositories.CommissionCalculationSessionRepository;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionService {
    private final CommissionPlanService commissionPlanService;
    private final CommissionCalculationSessionRepository commissionCalculationSessionRepository;
    private final CDBRepository cdbRepository;

    @Inject
    public CommissionCalculationSessionService(CommissionPlanService commissionPlanService, CommissionCalculationSessionRepository commissionCalculationSessionRepository, CDBRepository cdbRepository) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionRepository = commissionCalculationSessionRepository;
        this.cdbRepository = cdbRepository;
    }

    public CommissionCalculationSession validateExistCalculationSession(ObjectId calculationSessionId) {
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionRepository.findOne(calculationSessionId);
        if (commissionCalculationSession == null) {
            throw new CommissionCalculationSessionException("Not found calculation session " + calculationSessionId);
        }
        return commissionCalculationSession;
    }

    public CommissionCalculationSession calculateCommissionForPolicies(List<String> policyNumbers) {
        return null;
    }

}
