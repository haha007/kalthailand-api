package th.co.krungthaiaxa.elife.api.cdb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;

import javax.inject.Inject;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CDBUtilTest {

    @Inject
    private CDBUtil cdbUtil;

    @Test
    public void should_get_previous_pno_and_agent_code() throws Exception {
        Map<String, String> m = cdbUtil.getExistingAgentCode("3320300579936", "19690510");
        assertThat(m).hasSize(3);
    }

    @Test
    public void should_not_get_previous_pno_and_agent_code() throws Exception {
        Map<String, String> m = cdbUtil.getExistingAgentCode("", "");
        assertThat(m).isNull();
    }

}
