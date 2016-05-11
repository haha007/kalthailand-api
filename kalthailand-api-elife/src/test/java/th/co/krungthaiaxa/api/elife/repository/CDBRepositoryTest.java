package th.co.krungthaiaxa.api.elife.repository;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;
import java.util.Optional;

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
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode("0570189015328", "19760101");
        assertThat(agentCode.isPresent()).isTrue();
        assertThat(agentCode.get().getLeft()).isEqualTo("505-5284359");
        assertThat(agentCode.get().getMiddle()).isEqualTo("7019304046723");
        assertThat(agentCode.get().getRight()).isEqualTo("0");
    }

    @Test
    public void should_get_existing_agent_code_base_on_pid_field() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode("1000400014261", "19850408");
        assertThat(agentCode.isPresent()).isTrue();
        assertThat(agentCode.get().getLeft()).isEqualTo("506-6688036");
        assertThat(agentCode.get().getMiddle()).isEqualTo("1310804058903");
        assertThat(agentCode.get().getRight()).isEqualTo("1310804062950");
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_thai_id() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode("000", "19850408");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_date_of_birth() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode("1000400014261", "19000101");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_empty_parameters() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode("", "");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_null_parameters() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbRepository.getExistingAgentCode(null, null);
        assertThat(agentCode.isPresent()).isFalse();
    }

}
