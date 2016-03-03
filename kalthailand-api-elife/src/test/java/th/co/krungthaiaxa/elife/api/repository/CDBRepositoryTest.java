package th.co.krungthaiaxa.elife.api.repository;

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
public class CDBRepositoryTest {

    @Inject
    private CDBRepository cdbRepository;

    @Test
    public void should_get_existing_agent_code_base_on_pownid_field() throws Exception {
        Map<String,Object> m = cdbRepository.getExistingAgentCode("0570189015328","19760101");
        assertThat(m).isNotEmpty();
        assertThat(m.get("pno").toString()).isEqualTo("505-5284359");
        assertThat(m.get("pagt1").toString()).isEqualTo("7019304046723");
        assertThat(m.get("pagt2").toString()).isEqualTo("0");
    }

    @Test
    public void should_get_existing_agent_code_base_on_pid_field() throws Exception {
        Map<String,Object> m = cdbRepository.getExistingAgentCode("1000400014261","19850408");
        assertThat(m).isNotEmpty();
        assertThat(m.get("pno").toString()).isEqualTo("506-6688036");
        assertThat(m.get("pagt1").toString()).isEqualTo("1310804058903");
        assertThat(m.get("pagt2").toString()).isEqualTo("1310804062950");
    }

    @Test
    public void should_cannot_get_existing_agent_code() throws Exception {
        Map<String,Object> m = cdbRepository.getExistingAgentCode("000","000");
        assertThat(m).isEqualTo(null);
    }

    @Test
    public void should_handler_null_or_blank_input_parameter() throws Exception {
        Map<String,Object> m1 = cdbRepository.getExistingAgentCode("","");
        assertThat(m1).isEqualTo(null);
        Map<String,Object> m2 = cdbRepository.getExistingAgentCode(null,null);
        assertThat(m2).isEqualTo(null);
    }






}
