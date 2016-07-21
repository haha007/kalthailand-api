package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.pdf.PdfReader;
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
import th.co.krungthaiaxa.api.elife.model.PhoneNumber;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PolicySetting;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SettingServiceTest extends ELifeTest {
    public static final Logger logger = LoggerFactory.getLogger(SettingServiceTest.class);

    @Inject
    private SettingService settingService;

    @Test
    public void get_policy_setting_not_empty() throws Exception {
        PolicySetting policySetting = settingService.loadPolicySetting();
        Integer quota = policySetting.getQuota();
        logger.info("Quota Setting: {}", quota);
        Assert.assertNotNull(quota);
        Assert.assertTrue(quota > 0);
    }

}
