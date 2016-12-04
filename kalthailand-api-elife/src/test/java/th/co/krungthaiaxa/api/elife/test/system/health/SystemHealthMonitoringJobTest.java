package th.co.krungthaiaxa.api.elife.test.system.health;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final int JOB_SCHEDULE_IN_MILLS = 1000;

    @Autowired
    private SystemHealthMonitoringJob systemHealthMonitoringJob;
    @Autowired
    private SystemHealthSettingService systemHealthSettingService;

    private SystemHealthSetting originalSystemHealthSetting;
    private SystemHealthSetting testingSetting;

    @Before
    public void init() {
        clearGreenMail();

        originalSystemHealthSetting = systemHealthSettingService.loadSetting();

        testingSetting = new SystemHealthSetting();
        org.springframework.beans.BeanUtils.copyProperties(originalSystemHealthSetting, testingSetting);
        testingSetting.setUsedMemoryPercentageWarning(0.5f);
        systemHealthSettingService.saveSetting(testingSetting);
    }

    @After
    public void after() {
        systemHealthSettingService.saveSetting(originalSystemHealthSetting);
    }

    @Test
    public void test() {
        int numberOfAlerEmails = testingSetting.getWarningEmails().size();

        //In the first time, no alert yet.
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(0, greenMail.getReceivedMessages().length);

        //But it will alert in after 1 second.
        sleep(JOB_SCHEDULE_IN_MILLS + 1);
        systemHealthMonitoringJob.monitorHealth();
        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "emails/system-health-notification.html");
        Assert.assertEquals(numberOfAlerEmails, greenMail.getReceivedMessages().length);

        //It next second, it won't alert anymore.
        sleep(JOB_SCHEDULE_IN_MILLS + 1);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(numberOfAlerEmails, greenMail.getReceivedMessages().length);

        //It next second again, now it will alert.
        sleep(JOB_SCHEDULE_IN_MILLS + 1);
        systemHealthMonitoringJob.monitorHealth();
        Assert.assertEquals(numberOfAlerEmails + 1, greenMail.getReceivedMessages().length);
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
