package th.co.krungthaiaxa.api.elife.policyNumbersQuota.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberService;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberSettingService;

import javax.inject.Inject;

/**
 * @author khoi.tran on 7/26/16.
 *         This cronjob will check the current policy numbers, wether is more than the setting percentage or not.
 *         When the current policy numbers are more than setting percentage, we will send the notification email to users.
 */
@Service
public class PolicyNumbersQuotaCheckerService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyNumbersQuotaCheckerService.class);
    private final PolicyNumberService policyNumberService;
    private final PolicyNumberSettingService policyNumberSettingService;
    private final PolicyNumbersQuotaNotificationService policyNumbersQuotaNotificationService;

    @Inject
    public PolicyNumbersQuotaCheckerService(PolicyNumberService policyNumberService, PolicyNumberSettingService policyNumberSettingService, PolicyNumbersQuotaNotificationService policyNumbersQuotaNotificationService) {
        this.policyNumberService = policyNumberService;
        this.policyNumberSettingService = policyNumberSettingService;
        this.policyNumbersQuotaNotificationService = policyNumbersQuotaNotificationService;
    }

    /**
     * @return whether the current policies will over quota in the near future or not (current used percentage of policies numbers is over notification percentage or not).
     */
    public boolean checkEnoughRemainPolicyNumbers() {
        long totalPolicyNumbers = policyNumberService.countAllPolicyNumbers();
        if (totalPolicyNumbers == 0) {
            LOGGER.warn("The current totalPolicyNumbers is 0. No need to check.");
            return false;
        }
        long availablePolicyNumbers = policyNumberService.countAvailablePolicyNumbers();
        PolicyNumberSetting policyNumberSetting = policyNumberSettingService.loadSetting();
        PolicyNumbersQuotaCheckerResult checkerResult = isUsedPercentOverTriggerPercent(totalPolicyNumbers, availablePolicyNumbers, policyNumberSetting);
        if (checkerResult.isNearlyOverQuota()) {
            policyNumbersQuotaNotificationService.sendNotification(checkerResult);
            return true;
        } else {
            return false;
        }
    }

    private PolicyNumbersQuotaCheckerResult isUsedPercentOverTriggerPercent(long totalPolicyNumbers, long availablePolicyNumbers, PolicyNumberSetting policyNumberSetting) {
        double availablePercent = ((double) availablePolicyNumbers / totalPolicyNumbers) * 100;
        double usedPercent = 100 - availablePercent;
        int usedTriggerPercent = policyNumberSetting.getTriggerPercent();
        boolean isOverQuota = Math.round(usedPercent) >= usedTriggerPercent;
        return new PolicyNumbersQuotaCheckerResult(totalPolicyNumbers, availablePolicyNumbers, usedPercent, availablePercent, policyNumberSetting, isOverQuota);
    }

    public static class PolicyNumbersQuotaCheckerResult {
        private long totalPolicyNumbers;
        private long availablePolicyNumbers;
        private double usedPercent;
        private double availablePercent;
        private PolicyNumberSetting policyNumberSetting;
        private boolean overQuota;

        public PolicyNumbersQuotaCheckerResult(long totalPolicyNumbers, long availablePolicyNumbers, double usedPercent, double availablePercent, PolicyNumberSetting policyNumberSetting, boolean overQuota) {
            this.totalPolicyNumbers = totalPolicyNumbers;
            this.availablePolicyNumbers = availablePolicyNumbers;
            this.usedPercent = usedPercent;
            this.availablePercent = availablePercent;
            this.policyNumberSetting = policyNumberSetting;
            this.overQuota = overQuota;
        }

        public double getUsedPercent() {
            return usedPercent;
        }

        public void setUsedPercent(double usedPercent) {
            this.usedPercent = usedPercent;
        }

        public double getAvailablePercent() {
            return availablePercent;
        }

        public void setAvailablePercent(double availablePercent) {
            this.availablePercent = availablePercent;
        }

        public PolicyNumberSetting getPolicyNumberSetting() {
            return policyNumberSetting;
        }

        public void setPolicyNumberSetting(PolicyNumberSetting policyNumberSetting) {
            this.policyNumberSetting = policyNumberSetting;
        }

        public boolean isNearlyOverQuota() {
            return overQuota;
        }

        public void setOverQuota(boolean overQuota) {
            this.overQuota = overQuota;
        }

        public long getTotalPolicyNumbers() {
            return totalPolicyNumbers;
        }

        public void setTotalPolicyNumbers(long totalPolicyNumbers) {
            this.totalPolicyNumbers = totalPolicyNumbers;
        }

        public long getAvailablePolicyNumbers() {
            return availablePolicyNumbers;
        }

        public void setAvailablePolicyNumbers(long availablePolicyNumbers) {
            this.availablePolicyNumbers = availablePolicyNumbers;
        }
    }
}
