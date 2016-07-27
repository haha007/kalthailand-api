package th.co.krungthaiaxa.api.elife.policyNumbersQuota.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.PolicyNumbersQuotaNotification;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.repository.PolicyNumbersQuotaNotificationRepository;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.utils.IOUtil;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author khoi.tran on 7/26/16.
 *         This cronjob will check the current policy numbers, wether is more than the setting percentage or not.
 *         When the current policy numbers are more than setting percentage, we will send the notification to users (the notification can be mail or web notification, or mobile notification..., but now we only support email).
 */
@Service
public class PolicyNumbersQuotaNotificationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyNumbersQuotaNotificationService.class);

    private final EmailService emailService;
    private final PolicyNumbersQuotaNotificationRepository policyNumbersQuotaNotificationRepository;

    @Inject
    public PolicyNumbersQuotaNotificationService(EmailService emailService, PolicyNumbersQuotaNotificationRepository policyNumbersQuotaNotificationRepository) {
        this.emailService = emailService;
        this.policyNumbersQuotaNotificationRepository = policyNumbersQuotaNotificationRepository;
    }

    public void sendNotification(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        String emailContent = populateEmailContent(policyNumbersQuotaCheckerResult);
        List<String> notificationSettingEmails = policyNumbersQuotaCheckerResult.getPolicyNumberSetting().getEmailList();
        long notificationTriggerSeconds = 1000;//TODO just for testing
        for (String email : notificationSettingEmails) {
            Optional<PolicyNumbersQuotaNotification> policyNumbersQuotaNotificationOptional = policyNumbersQuotaNotificationRepository.findOneByNotificationEmail();
            PolicyNumbersQuotaNotification policyNumbersQuotaNotification = policyNumbersQuotaNotificationOptional.orElse(initCurrentPolicyNumbersQuotaNotification(email));
            if (isOverNotificationDuration(policyNumbersQuotaNotification, notificationTriggerSeconds)) {
                emailService.sendEmail(email, "Available Policy Numbers will be no more soon.", emailContent);
                policyNumbersQuotaNotificationRepository.save(policyNumbersQuotaNotification);
            }
        }
    }

    private PolicyNumbersQuotaNotification initCurrentPolicyNumbersQuotaNotification(String email) {
        PolicyNumbersQuotaNotification policyNumbersQuotaNotification = new PolicyNumbersQuotaNotification();
        policyNumbersQuotaNotification.setNotificationEmail(email);
        policyNumbersQuotaNotification.setNotificationTime(ZonedDateTime.now());
        return policyNumbersQuotaNotification;
    }

    /**
     * @param policyNumbersQuotaNotification
     * @param notificationTriggerSeconds     the duration of notification.
     * @return check whether the email was sent before the input @beforeSeconds or not.
     */
    private boolean isOverNotificationDuration(PolicyNumbersQuotaNotification policyNumbersQuotaNotification, long notificationTriggerSeconds) {
        ZonedDateTime lastNotificationTime = policyNumbersQuotaNotification.getNotificationTime();
        long secondsFromLastNotification;
        if (lastNotificationTime == null) {
            secondsFromLastNotification = lastNotificationTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
        } else {
            secondsFromLastNotification = 0;
        }
        return secondsFromLastNotification >= notificationTriggerSeconds;
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
