package th.co.krungthaiaxa.api.elife.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.elife.utils.ObjectMapperUtil;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
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
        Assert.assertTrue(!policySetting.getEmailList().isEmpty());
    }


}
