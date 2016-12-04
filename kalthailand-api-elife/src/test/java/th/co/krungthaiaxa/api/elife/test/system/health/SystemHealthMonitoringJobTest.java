package th.co.krungthaiaxa.api.elife.test.system.health;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealthMonitoringJob;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealthSetting;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealthSettingService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.GreenMailUtil;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

/**
 * @author khoi.tran on 12/4/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SystemHealthMonitoringJobTest extends ELifeTest {
    //Defined in application-test.properties, 'system.health.warning.in-danger.mills'
    @Value("${system.health.warning.in-danger.mills}")
    private int MILLS_IN_DANGER;

    @Autowired
    private SystemHealthMonitoringJob systemHealthMonitoringJob;
    @Autowired
    private SystemHealthSettingService systemHealthSettingService;

    private SystemHealthSetting originalSystemHealthSetting;

    @Before
    public void init() {
        clearGreenMail();

        originalSystemHealthSetting = systemHealthSettingService.loadSetting();

    }

    @After
    public void after() {
        systemHealthSettingService.saveSetting(originalSystemHealthSetting);
    }

    @Test
    public void test_in_danger_memory() {
        SystemHealthSetting testingSetting = cloneSettingFromOriginal();
        testingSetting.setUsedMemoryPercentageWarning(0.001f);
        testInDanger(testingSetting);
    }

    @Test
    public void test_in_danger_space() {
        SystemHealthSetting testingSetting = cloneSettingFromOriginal();
        testingSetting.setUsedSpacePercentageWarning(0.001f);
        testInDanger(testingSetting);
    }

    private SystemHealthSetting cloneSettingFromOriginal() {
        SystemHealthSetting testingSetting = new SystemHealthSetting();
        org.springframework.beans.BeanUtils.copyProperties(originalSystemHealthSetting, testingSetting);
        return testingSetting;
    }

    private void testInDanger(SystemHealthSetting inDangerSystemHealthSetting) {
        systemHealthSettingService.saveSetting(inDangerSystemHealthSetting);
        int numberOfAlerEmails = inDangerSystemHealthSetting.getWarningEmails().size();

        //In the first time, no alert yet.
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(0, greenMail.getReceivedMessages().length);

        //Wait 1 time: Alert now, but next round you have to wait (1*2)
        sleep(MILLS_IN_DANGER + 1);
        systemHealthMonitoringJob.monitorHealth();
        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "emails/system-health-notification.html");
        Assert.assertEquals(numberOfAlerEmails, greenMail.getReceivedMessages().length);

        //Wait 1 time: Not alert ( 1 < 1*2)
        sleep(MILLS_IN_DANGER + 1);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(numberOfAlerEmails, greenMail.getReceivedMessages().length);

        //Wait 2 times: Alert (2 >= 1*2), the next round will be 2*2
        sleep(MILLS_IN_DANGER + 1);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(numberOfAlerEmails + 1, greenMail.getReceivedMessages().length);

        //After 4th second, the alert will be reset to 1st second.
        //Reset systemHealSetting to original: don't alert anymore.
        systemHealthSettingService.saveSetting(originalSystemHealthSetting);
        systemHealthMonitoringJob.monitorHealth();
        sleep(MILLS_IN_DANGER * 4 + 1);

        //THE ALERT TIMER WAS RESET TO 1 UNIT /////////////////////
        //In the first time, no alert yet.
        clearGreenMail();
        systemHealthSettingService.saveSetting(inDangerSystemHealthSetting);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(0, greenMail.getReceivedMessages().length);

        //Wait 1 time: Alert now, but next round you have to wait (1*2)
        sleep(MILLS_IN_DANGER + 1);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(numberOfAlerEmails, greenMail.getReceivedMessages().length);

        //RESET ALERT TIMER FOR OTHER TESTING
        sleep(MILLS_IN_DANGER + 1);
        systemHealthSettingService.saveSetting(originalSystemHealthSetting);
        systemHealthMonitoringJob.monitorHealth();
    }

    private void sleep(int mills) {
        try {
            //longer than cron job scheduled (defined in property 'system.health.cron.interval.seconds')
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
