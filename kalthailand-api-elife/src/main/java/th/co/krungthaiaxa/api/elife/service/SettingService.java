package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.Setting;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.repository.*;

import javax.inject.Inject;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;

@Service
public class SettingService {
    private final static Logger logger = LoggerFactory.getLogger(SettingService.class);
    private static final int POLICY_QUOTA = 1000;
    @Inject
    private SettingRepository settingRepository;

    /**
     * This method is not only find policy setting. If it cannot find any setting inside DB, then it will initiate one setting in DB.
     *
     * @return
     */
    public PolicySetting loadPolicySetting() {
        Setting setting;
        List<Setting> settings = settingRepository.findAll();
        if (settings.isEmpty()) {
            setting = initDefaultSetting();
            settingRepository.save(setting);
        } else {
            if (settings.size() > 1) {
                logger.warn("We actually use only the first one setting. Other settings will be ingnored.");
            }
            setting = settings.get(0);
        }
        PolicySetting policySetting = setting.getPolicySetting();
        if (policySetting == null) {
            setting.setPolicySetting(initDefaultPolicySetting());
            settingRepository.save(setting);
        }
        return policySetting;
    }

    private Setting initDefaultSetting() {
        Setting setting = new Setting();
        setting.setPolicySetting(initDefaultPolicySetting());
        return setting;
    }

    private PolicySetting initDefaultPolicySetting() {
        PolicySetting policySetting = new PolicySetting();
        policySetting.setQuota(POLICY_QUOTA);
        return policySetting;
    }
}
