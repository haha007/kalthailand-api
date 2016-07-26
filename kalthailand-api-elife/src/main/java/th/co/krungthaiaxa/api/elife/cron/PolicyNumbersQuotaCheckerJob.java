package th.co.krungthaiaxa.api.elife.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author khoi.tran on 7/26/16.
 *         This cronjob will check the current policy numbers, wether is more than the setting percentage or not.
 *         When the current policy numbers are more than setting percentage, we will send the notification email to users.
 */
@Service
public class PolicyNumbersQuotaCheckerJob {

    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyNumbersQuotaCheckerJob.class);
    private final PolicyNumbersQuotaCheckerService policyNumbersQuotaCheckerService;

    @Inject
    public PolicyNumbersQuotaCheckerJob(PolicyNumbersQuotaCheckerService policyNumbersQuotaCheckerService) {this.policyNumbersQuotaCheckerService = policyNumbersQuotaCheckerService;}

    public void execute() {
        policyNumbersQuotaCheckerService.checkEnoughRemainPolicyNumbers();
    }

}
