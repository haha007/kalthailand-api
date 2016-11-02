package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.incremental.IncrementalService;

/**
 * @author khoi.tran on 11/2/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IncrementalServiceTest {
    @Autowired
    private IncrementalService incrementalService;

    @Test
    public void test_id_of_new_key_start_by_0_and_then_increase_to_1() {
        String key = "TEST_KEY_" + DateTimeUtil.formatNowForFilePath();
        long nextId = incrementalService.next(key);
        Assert.assertEquals(0, nextId);

        nextId = incrementalService.next(key);
        Assert.assertEquals(1, nextId);
    }
    @Test
    public void test_multi_thread_increase_correctly() {

    }
}
