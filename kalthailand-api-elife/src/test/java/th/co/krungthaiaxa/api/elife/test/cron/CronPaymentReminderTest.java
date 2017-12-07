package th.co.krungthaiaxa.api.elife.test.cron;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.cron.CronPaymentReminder;

import javax.inject.Inject;

/**
 * @author tuong.le on 11/29/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CronPaymentReminderTest {
    @Inject
    private CronPaymentReminder cronPaymentReminder;
    
    @Test
    public void should_send_all_payment_reminder_to_customer(){
        cronPaymentReminder.cronPaymentReminder();
        Assert.assertTrue(true);
    }
}
