package th.co.krungthaiaxa.api.elife.system.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.NumberUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;

import java.util.List;

/**
 * @author khoi.tran on 11/26/16.
 */
@Service
public class SystemHealthMonitoringJob {
    private static final long BYTES_TO_GB = 1073741824;
    private final SystemHealthService systemHealthService;
    private final SystemHealthSettingService systemHealthSettingService;
    private final ElifeEmailService emailService;

    private static final String EMAIL_HEALTH_WARNING_PATH = "/system/health/email-health-warning.html";
    private static final String EMAIL_HEALTH_WARNING_TEMPLATE = IOUtil.loadTextFileInClassPath(EMAIL_HEALTH_WARNING_PATH);

    @Autowired
    public SystemHealthMonitoringJob(SystemHealthService systemHealthService, ElifeEmailService emailService, SystemHealthSettingService systemHealthSettingService) {
        this.systemHealthService = systemHealthService;
        this.systemHealthSettingService = systemHealthSettingService;
        this.emailService = emailService;
    }

//    @PostConstruct
//    public void init() {
//        systemHealthSettingService.loadSetting();
//    }

    @Scheduled(fixedRateString = "${system.health.cron.interval.seconds}000")
    public void monitorHealth() {
        SystemHealthSetting systemHealthSetting = systemHealthSettingService.loadSetting();
        SystemHealth systemHealth = systemHealthService.loadHealthStatus();
        if (systemHealth.getJvmUsedMemoryPercentage() > systemHealthSetting.getUsedMemoryPercentageWarning()) {
            LogUtil.logStarting("[SYSTEM.HEALTH] [WARNING]" + ObjectMapperUtil.toStringMultiLine(systemHealth));
            sendWarningEmail(systemHealth, systemHealthSetting);
        }
    }

    private void sendWarningEmail(SystemHealth systemHealth, SystemHealthSetting systemHealthSetting) {
        String emailHealthWarningContent = EMAIL_HEALTH_WARNING_TEMPLATE;
        emailHealthWarningContent = emailHealthWarningContent
                .replaceAll("%USED_MEMORY%", NumberUtil.formatCurrencyValue(systemHealth.getJvmUsedMemory() / BYTES_TO_GB))
                .replaceAll("%USED_MEMORY_PERCENTAGE%", "" + systemHealth.getJvmUsedMemoryPercentage());
        List<String> toEmails = systemHealthSetting.getWarningEmails();
        for (String toEmail : toEmails) {
            emailService.sendEmail(toEmail, "[Elife][System Health] warning health", emailHealthWarningContent);
        }
    }
}
