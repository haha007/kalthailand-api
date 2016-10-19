package th.co.krungthaiaxa.api.elife;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * TODO should remove
 *
 * @deprecated This class seem not to be used any more.
 */
@Deprecated
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class KalApiApplicationTests {

    @Test
    public void contextLoads() {
    }

}
