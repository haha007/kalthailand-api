package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.LineBC;
import th.co.krungthaiaxa.api.elife.service.LineBCService;

import javax.inject.Inject;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineBCServiceTest extends ELifeTest {

    @Inject
    private LineBCService lineBCService;

    @Test
    public void should_get_line_bc_information_along_with_mid() {
        Optional<LineBC> data = lineBCService.getLineBCInfo("u53cb613d9269dd6875f60249402b4542");
        assertThat(data.get().getDob()).isEqualTo("30/11/1976");
        assertThat(data.get().getEmail()).isEqualTo("Pimpaporn_a@hotmail.com");
        assertThat(data.get().getFirstName()).isEqualTo("พิมพมภรณ์");
        assertThat(data.get().getLastName()).isEqualTo("อาภาศิริผล");
        assertThat(data.get().getMobile()).isEqualTo("0815701554");
        assertThat(data.get().getPid()).isEqualTo("3100902286661");
    }

    @Test
    public void should_return_optional_empty() {
        Optional<LineBC> data = lineBCService.getLineBCInfo("somethingWrong");
        assertThat(data).isEmpty();
    }

}
