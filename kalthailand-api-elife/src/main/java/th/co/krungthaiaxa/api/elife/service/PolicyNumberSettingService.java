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

    private final PolicyNumberSettingRepository policyNumberSettingRepository;

    @Inject
    public PolicyNumberSettingService(PolicyNumberSettingRepository policyNumberSettingRepository) {this.policyNumberSettingRepository = policyNumberSettingRepository;}

    public PolicyNumberSetting loadPolicyNumberSetting() {
        PolicyNumberSetting setting;
        List<PolicyNumberSetting> settings = policyNumberSettingRepository.findAll();
        if (settings.isEmpty()) {
            setting = initDefaultPolicyNumberNotificationSetting();
            policyNumberSettingRepository.save(setting);
        } else {
            if (settings.size() > 1) {
                logger.warn("We actually use only the first one setting. Other settings will be ingnored.");
            }
            setting = settings.get(0);
        }
        return setting;
    }

    public PolicyNumberSetting updatePolicyNumberSetting(PolicyNumberSetting newPolicyNumberSetting) {
        PolicyNumberSetting policyNumberSetting = loadPolicyNumberSetting();
        BeanUtils.copyProperties(newPolicyNumberSetting, policyNumberSetting, "id");
        return policyNumberSettingRepository.save(policyNumberSetting);
    }

    private PolicyNumberSetting initDefaultPolicyNumberNotificationSetting() {
        PolicyNumberSetting policyNumberSetting = new PolicyNumberSetting();
        policyNumberSetting.setTriggerPercent(100);
        policyNumberSetting.setEmailList(Collections.EMPTY_LIST);
        policyNumberSetting.setTimeTrigger(3600);
        return policyNumberSetting;
    }

}
