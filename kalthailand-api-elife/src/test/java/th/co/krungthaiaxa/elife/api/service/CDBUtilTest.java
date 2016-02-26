package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.cdb.CDBUtil;

import javax.inject.Inject;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.beneficiary;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.insured;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.quote;

/**
 * Created by santilik on 2/26/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CDBUtilTest {

    @Inject
    private CDBUtil CDBUtil;

    @Test
    public void should_get_previous_pno_and_agent_code() throws Exception {
        Map<String,String> m = null;
        m = CDBUtil.getExistingAgentCode("3320300579936","19690510");
        assertThat(m).hasSize(3);
    }

    @Test
    public void should_not_get_previous_pno_and_agent_code() throws Exception {
        Map<String,String> m = null;
        m = CDBUtil.getExistingAgentCode("","");
        assertThat(m==null);
    }

}
