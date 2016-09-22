package th.co.krungthaiaxa.api.elife.commission.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;

import javax.inject.Inject;

/**
 * @author khoi.tran on 9/5/16.
 */
@Service
public class CommissionCalculationSessionExportService {
    private final CommissionCalculationSessionService commissionCalculationSessionService;

    @Inject
    public CommissionCalculationSessionExportService(CommissionCalculationSessionService commissionCalculationSessionService) {this.commissionCalculationSessionService = commissionCalculationSessionService;}

    public byte[] exportExcel(String commissionCalculationSessionId) {
        ObjectId objectId = new ObjectId(commissionCalculationSessionId);
        CommissionCalculationSession commissionCalculationSession = commissionCalculationSessionService.validateExistCalculationSession(objectId);
        //TODO this method never return null.
        return null;
    }

}
