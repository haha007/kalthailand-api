package th.co.krungthaiaxa.api.elife.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.utils.IOUtil;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 7/26/16.
 *         This cronjob will check the current policy numbers, wether is more than the setting percentage or not.
 *         When the current policy numbers are more than setting percentage, we will send the notification email to users.
 */
@Service
public class PolicyNumbersQuotaEmailService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyNumbersQuotaEmailService.class);

    private final EmailService emailService;

    @Inject
    public PolicyNumbersQuotaEmailService(EmailService emailService) {this.emailService = emailService;}

    public void sendNotification(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        String emailContent = populateEmailContent(policyNumbersQuotaCheckerResult);
        String toEmails = policyNumbersQuotaCheckerResult.getPolicyNumberSetting().getEmailList().stream().collect(Collectors.joining(", "));
        emailService.sendEmail(toEmails, "Available Policy Numbers will be no more soon.", emailContent);
    }

    private String populateEmailContent(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        String emailContent = IOUtil.loadTextFile("/email-content/email-policy-numbers-over-quota.html");
        emailContent = emailContent.replace("%TOTAL_POLICY_NUMBERS%", "" + policyNumbersQuotaCheckerResult.getTotalPolicyNumbers());
        long usedPolicyNumbers = policyNumbersQuotaCheckerResult.getTotalPolicyNumbers() - policyNumbersQuotaCheckerResult.getAvailablePolicyNumbers();
        emailContent = emailContent.replace("%USED_POLICY_NUMBERS%", "" + usedPolicyNumbers);
        emailContent = emailContent.replace("%USED_POLICY_NUMBERS_PERCENT%", "" + policyNumbersQuotaCheckerResult.getUsedPercent());
        emailContent = emailContent.replace("%TRIGGER_POLICY_NUMBERS_PERCENT%", "" + policyNumbersQuotaCheckerResult.getPolicyNumberSetting().getTriggerPercent());
        return emailContent;
    }
}
