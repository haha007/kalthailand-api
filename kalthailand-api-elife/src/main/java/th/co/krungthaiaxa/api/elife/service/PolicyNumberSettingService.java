package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberSettingRepository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Service
public class PolicyNumberSettingService {
    private final static Logger logger = LoggerFactory.getLogger(PolicyNumberSettingService.class);
    public static final int DEFAULT_TRIGGER_PERCENT = 80;
    public static final long DEFAULT_TRIGGER_SECONDS = 3600l;//1 hour

    private final PolicyNumberSettingRepository policyNumberSettingRepository;

    @Inject
    public PolicyNumberSettingService(PolicyNumberSettingRepository policyNumberSettingRepository) {this.policyNumberSettingRepository = policyNumberSettingRepository;}

    public PolicyNumberSetting loadSetting() {
        PolicyNumberSetting setting;
        List<PolicyNumberSetting> settings = policyNumberSettingRepository.findAll();
        if (settings.isEmpty()) {
            setting = initDefaultPolicyNumberNotificationSetting();
            policyNumberSettingRepository.save(setting);
        } else {
            if (settings.size() > 1) {
                logger.trace("We actually use only the first one setting. Other settings will be ingnored.");
            }
            setting = settings.get(0);
        }
        return setting;
    }

    public PolicyNumberSetting updateSetting(PolicyNumberSetting newPolicyNumberSetting) {
        PolicyNumberSetting policyNumberSetting = loadSetting();
        BeanUtils.copyProperties(newPolicyNumberSetting, policyNumberSetting, "id");
        return policyNumberSettingRepository.save(policyNumberSetting);
    }

    private PolicyNumberSetting initDefaultPolicyNumberNotificationSetting() {
        PolicyNumberSetting policyNumberSetting = new PolicyNumberSetting();
        policyNumberSetting.setTriggerPercent(DEFAULT_TRIGGER_PERCENT);
        policyNumberSetting.setEmailList(Collections.EMPTY_LIST);
        policyNumberSetting.setTimeTrigger(DEFAULT_TRIGGER_SECONDS);
        return policyNumberSetting;
    }

    public PolicyNumberSetting updateDefaultTriggerTime(PolicyNumberSetting policyNumberSetting) {
        policyNumberSetting.setTimeTrigger(DEFAULT_TRIGGER_SECONDS);
        return updateSetting(policyNumberSetting);
    }
}
