package th.co.krungthaiaxa.api.elife.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberService;

import javax.inject.Inject;

/**
 * @author khoi.tran on 7/26/16.
 *         This cronjob will check the current policy numbers, wether is more than the setting percentage or not.
 *         When the current policy numbers are more than setting percentage, we will send the notification email to users.
 */
@Component("checkEnoughPolicyNumbers")
public class CheckEnoughPolicyNumbers {
    public static final Logger LOGGER = LoggerFactory.getLogger(CheckEnoughPolicyNumbers.class);
    private final PolicyNumberService policyNumberService;

    @Inject
    public CheckEnoughPolicyNumbers(PolicyNumberService policyNumberService) {
        this.policyNumberService = policyNumberService;
    }

    public void execute() {
        checkEnoughRemainPolicyNumbers();
    }

    public static int INT = 0;

    public void checkEnoughRemainPolicyNumbers() {
        LOGGER.info("************ TEST A' ******************** " + INT++);
    }
}
