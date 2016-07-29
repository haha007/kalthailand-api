package th.co.krungthaiaxa.api.elife.policyNumbersQuota.service;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.elife.data.PolicyNumbersQuotaNotification;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.repository.PolicyNumbersQuotaNotificationRepository;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberSettingService;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * @author khoi.tran on 7/26/16.
 *         When the current policy numbers are more than setting percentage, we will send the notification to users (the notification can be mail or web notification, or mobile notification..., but now we only support email).
 */
@Service
public class PolicyNumbersQuotaNotificationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyNumbersQuotaNotificationService.class);
    private final EmailService emailService;
    private final PolicyNumbersQuotaNotificationRepository policyNumbersQuotaNotificationRepository;
    private final PolicyNumberSettingService policyNumberSettingService;

    @Value("${policynumbersquota.cron.time.scale}")
    private long timeScale;

    @Inject
    public PolicyNumbersQuotaNotificationService(EmailService emailService, PolicyNumbersQuotaNotificationRepository policyNumbersQuotaNotificationRepository, PolicyNumberSettingService policyNumberSettingService) {
        this.emailService = emailService;
        this.policyNumbersQuotaNotificationRepository = policyNumbersQuotaNotificationRepository;
        this.policyNumberSettingService = policyNumberSettingService;
    }

    public void sendNotification(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        String emailContent = populateEmailContent(policyNumbersQuotaCheckerResult);
        List<Pair<byte[], String>> imagesPairs = emailService.getDefaultImagePairs();

        PolicyNumberSetting policyNumberSetting = policyNumbersQuotaCheckerResult.getPolicyNumberSetting();
        List<String> notificationSettingEmails = policyNumberSetting.getEmailList();
        long notificationTriggerSeconds = getNotificationTriggerSecondsFromSetting(policyNumbersQuotaCheckerResult);
        for (String email : notificationSettingEmails) {
            Optional<PolicyNumbersQuotaNotification> policyNumbersQuotaNotificationOptional = policyNumbersQuotaNotificationRepository.findOneByNotificationEmail(email);
            PolicyNumbersQuotaNotification policyNumbersQuotaNotification = policyNumbersQuotaNotificationOptional.orElse(initCurrentPolicyNumbersQuotaNotification(email));
            if (isOverNotificationDuration(policyNumbersQuotaNotification, notificationTriggerSeconds)) {
                emailService.sendEmail(email, "Available Policy Numbers will be no more soon.", emailContent, imagesPairs);
                policyNumbersQuotaNotification.setNotificationTime(Instant.now());
                policyNumbersQuotaNotificationRepository.save(policyNumbersQuotaNotification);
            }
        }
    }

    /**
     * This method will ensure that the trigger time is never null.
     * If the setting doesn't have this value, it will update to the setting.
     *
     * @param policyNumbersQuotaCheckerResult
     * @return
     */
    private long getNotificationTriggerSecondsFromSetting(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        PolicyNumberSetting policyNumberSetting = policyNumbersQuotaCheckerResult.getPolicyNumberSetting();
        Long notificationTriggerSeconds = policyNumberSetting.getTimeTrigger();
        if (notificationTriggerSeconds == null || notificationTriggerSeconds <= 0) {
            policyNumberSetting = policyNumberSettingService.updateDefaultTriggerTime(policyNumberSetting);
            policyNumbersQuotaCheckerResult.setPolicyNumberSetting(policyNumberSetting);
            LOGGER.warn("There's no triggerTime in PolicyNumberSetting, so we will update the setting with default triggerTime");
            notificationTriggerSeconds = policyNumberSetting.getTimeTrigger();
        }
        return notificationTriggerSeconds;
    }

    private PolicyNumbersQuotaNotification initCurrentPolicyNumbersQuotaNotification(String email) {
        PolicyNumbersQuotaNotification policyNumbersQuotaNotification = new PolicyNumbersQuotaNotification();
        policyNumbersQuotaNotification.setNotificationEmail(email);
        policyNumbersQuotaNotification.setNotificationTime(null);
        return policyNumbersQuotaNotification;
    }

    /**
     * @param policyNumbersQuotaNotification
     * @param notificationTriggerSeconds     the duration of notification.
     * @return check whether the email was sent before the input @beforeSeconds or not.
     */
    private boolean isOverNotificationDuration(PolicyNumbersQuotaNotification policyNumbersQuotaNotification, long notificationTriggerSeconds) {
        Instant lastNotificationTime = policyNumbersQuotaNotification.getNotificationTime();
        long secondsFromLastNotification;
        if (lastNotificationTime == null) {
            return true;
        } else {
            secondsFromLastNotification = Instant.now().getEpochSecond() - lastNotificationTime.getEpochSecond();
            return secondsFromLastNotification * timeScale >= notificationTriggerSeconds;
        }
    }

    private String populateEmailContent(PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult policyNumbersQuotaCheckerResult) {
        String emailContent = IOUtil.loadTextFile("/email-content/email-policy-numbers-over-quota.html");
        emailContent = emailContent.replace("%TOTAL_POLICY_NUMBERS%", "" + policyNumbersQuotaCheckerResult.getTotalPolicyNumbers());
        long usedPolicyNumbers = policyNumbersQuotaCheckerResult.getTotalPolicyNumbers() - policyNumbersQuotaCheckerResult.getAvailablePolicyNumbers();
        emailContent = emailContent.replace("%USED_POLICY_NUMBERS%", "" + usedPolicyNumbers);
        emailContent = emailContent.replace("%USED_POLICY_NUMBERS_PERCENT%", String.format("%.2f", policyNumbersQuotaCheckerResult.getUsedPercent()));
        emailContent = emailContent.replace("%TRIGGER_POLICY_NUMBERS_PERCENT%", String.format("%.2f", policyNumbersQuotaCheckerResult.getPolicyNumberSetting().getTriggerPercent()));
        return emailContent;
    }
}
