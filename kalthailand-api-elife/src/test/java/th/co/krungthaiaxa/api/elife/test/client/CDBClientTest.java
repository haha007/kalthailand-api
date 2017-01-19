package th.co.krungthaiaxa.api.elife.test.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.CDBClient;
import th.co.krungthaiaxa.api.elife.model.PreviousPolicy;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CDBClientTest extends ELifeTest {
    @Inject
    private CDBClient cdbClient;

    @Test
    public void should_get_existing_agent_code_base_on_pid_field() throws Exception {
        Optional<PreviousPolicy> agentCode = cdbClient.getExistingAgentCode("existingThaiId", "existingDOB");
        assertThat(agentCode.isPresent()).isTrue();
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_thai_id() throws Exception {
        Optional<PreviousPolicy> agentCode = cdbClient.getExistingAgentCode("something", "existingDOB");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_not_existing_date_of_birth() throws Exception {
        Optional<PreviousPolicy> agentCode = cdbClient.getExistingAgentCode("1000400014261", "something");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_empty_parameters() throws Exception {
        Optional<PreviousPolicy> agentCode = cdbClient.getExistingAgentCode("", "");
        assertThat(agentCode.isPresent()).isFalse();
    }

    @Test
    public void should_not_get_existing_agent_code_with_null_parameters() throws Exception {
        Optional<PreviousPolicy> agentCode = cdbClient.getExistingAgentCode(null, null);
        assertThat(agentCode.isPresent()).isFalse();
    }

}
