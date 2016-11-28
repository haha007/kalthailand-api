package th.co.krungthaiaxa.api.elife.system.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.StringUtil;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSetting;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSettingService;

/**
 * @author khoi.tran on 11/26/16.
 */
@Service
public class SystemHealthSettingService {
    private final GeneralSettingService generalSettingService;

    @Value("${system.health.warning.to-emails}")
    private String warningToEmails;
    @Value("${system.health.warning.used-memory-percentage}")
    private Float usedMemoryPercentageWarning;
    @Value("${system.health.warning.used-space-percentage}")
    private Float usedSpacePercentageWarning;

    @Autowired
    public SystemHealthSettingService(GeneralSettingService generalSettingService) {this.generalSettingService = generalSettingService;}

    public SystemHealthSetting loadSetting() {
        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        SystemHealthSetting systemHealthSetting = generalSetting.getSystemHealthSetting();
        if (systemHealthSetting == null) {
            systemHealthSetting = new SystemHealthSetting();
            systemHealthSetting.setWarningEmails(StringUtil.splitToNotNullStrings(warningToEmails));
            systemHealthSetting.setUsedMemoryPercentageWarning(usedMemoryPercentageWarning);
            systemHealthSetting.setUsedSpacePercentageWarning(usedSpacePercentageWarning);
            generalSetting.setSystemHealthSetting(systemHealthSetting);
            generalSettingService.saveSetting(generalSetting);
        }
        return systemHealthSetting;
    }

    public SystemHealthSetting saveSetting(SystemHealthSetting systemHealthSetting) {
        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        generalSetting.setSystemHealthSetting(systemHealthSetting);
        generalSettingService.saveSetting(generalSetting);
        return systemHealthSetting;
    }
}
