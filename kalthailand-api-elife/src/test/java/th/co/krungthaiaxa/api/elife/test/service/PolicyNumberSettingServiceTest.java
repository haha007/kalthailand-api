package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberSettingService;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyNumberSettingServiceTest extends ELifeTest {
    public static final Logger logger = LoggerFactory.getLogger(PolicyNumberSettingServiceTest.class);

    @Inject
    private PolicyNumberSettingService policyNumberSettingService;

    @Test
    public void get_policy_setting_not_empty() throws Exception {
        PolicyNumberSetting policySetting = policyNumberSettingService.loadSetting();
        logger.info("Quota Setting: {}", ObjectMapperUtil.toStringMultiLine(policySetting));
        Assert.assertNotNull(policySetting);
        Assert.assertTrue(policySetting.getTriggerPercent() > 0);
        Assert.assertTrue(policySetting.getTriggerPercent() <= 100);
    }

}
