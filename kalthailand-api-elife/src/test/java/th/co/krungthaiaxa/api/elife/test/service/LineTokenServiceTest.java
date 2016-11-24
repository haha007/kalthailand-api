package th.co.krungthaiaxa.api.elife.test.service;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.LineTokenService;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineTokenServiceTest {
	
	@Inject
    private LineTokenService lineTokenService;
	
	@Test
    public void should_get_line_token(){
		LineToken lineToken = lineTokenService.getLineToken();
		assertThat(lineToken).isNotNull();
	}
	
}
