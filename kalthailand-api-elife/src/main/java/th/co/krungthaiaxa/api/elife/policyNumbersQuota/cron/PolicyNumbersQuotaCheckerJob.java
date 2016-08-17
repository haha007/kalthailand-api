package th.co.krungthaiaxa.api.elife.policyNumbersQuota.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.service.PolicyNumbersQuotaCheckerService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @Value("${policynumbersquota.cron.interval.seconds}")
    private String jobInterval;

    @PostConstruct
    public void init() {
        LOGGER.debug("Job {}: start, interval: {} (s)", this.getClass().getSimpleName(), jobInterval);
    }

    @PreDestroy
    public void close() {
        LOGGER.debug("Job {}: close, interval: {} (s)", this.getClass().getSimpleName(), jobInterval);
    }

    @Scheduled(fixedRateString = "${policynumbersquota.cron.interval.seconds}000")
    public void execute() {
        LOGGER.debug("Execute task");
        PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult checkResult = policyNumbersQuotaCheckerService.checkEnoughRemainPolicyNumbers();
        LOGGER.debug("The current policy numbers is check wether over trigger percentage: {}", ObjectMapperUtil.toStringMultiLine(checkResult));
    }

}
