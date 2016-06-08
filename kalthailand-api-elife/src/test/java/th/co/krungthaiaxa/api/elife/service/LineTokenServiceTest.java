package th.co.krungthaiaxa.api.elife.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.LineToken;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
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
