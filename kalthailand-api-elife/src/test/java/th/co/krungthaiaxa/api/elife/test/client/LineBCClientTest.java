package th.co.krungthaiaxa.api.elife.test.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.LineBCClient;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineBCClientTest extends ELifeTest {
    @Inject
    private LineBCClient lineBCClient;

    @Test
    public void should_get_line_bc_information() {
        Optional<List<Map<String, Object>>> data = lineBCClient.getLineBC("u53cb613d9269dd6875f60249402b4542");
        assertThat(data.get()).hasSize(1);
        assertThat((String) data.get().get(0).get("dob")).isEqualTo("30/11/1976");
        assertThat((String) data.get().get(0).get("pid")).isEqualTo("3100902286661");
        assertThat((String) data.get().get(0).get("mobile")).isEqualTo("0815701554");
        assertThat((String) data.get().get(0).get("email")).isEqualTo("Pimpaporn_a@hotmail.com");
        assertThat((String) data.get().get(0).get("first_name")).isEqualTo("พิมพมภรณ์");
        assertThat((String) data.get().get(0).get("last_name")).isEqualTo("อาภาศิริผล");
    }

    @Test
    public void should_return_optional_empty() {
        Optional<List<Map<String, Object>>> data = lineBCClient.getLineBC("something");
        assertThat(data).isEqualTo(Optional.empty());
    }

}
