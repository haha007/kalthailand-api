package th.co.krungthaiaxa.api.elife.client;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CDBClientTest extends ELifeTest {
    @Inject
    private CDBClient cdbClient;

    @Test
    public void should_get_existing_agent_code_base_on_pid_field() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbClient.getExistingAgentCode("existingThaiId", "existingDOB");
        assertThat(agentCode.isPresent()).isTrue();
        assertThat(agentCode.get().getLeft()).isEqualTo("previousPolicyNumber");
        assertThat(agentCode.get().getMiddle()).isEqualTo("agentCode1");
        assertThat(agentCode.get().getRight()).isEqualTo("agentCode2");
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_thai_id() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbClient.getExistingAgentCode("something", "existingDOB");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_date_of_birth() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbClient.getExistingAgentCode("1000400014261", "something");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_empty_parameters() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbClient.getExistingAgentCode("", "");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_null_parameters() throws Exception {
        Optional<Triple<String, String, String>> agentCode = cdbClient.getExistingAgentCode(null, null);
        assertThat(agentCode.isPresent()).isFalse();
    }

}
