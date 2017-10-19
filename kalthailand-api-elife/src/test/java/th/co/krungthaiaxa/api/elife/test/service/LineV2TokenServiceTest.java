package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.v2.service.LineTokenService;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineV2TokenServiceTest {

    @Inject
    private LineTokenService lineTokenServiceV2;

    @Test
    public void should_reissue_line__access_token() {
        final LineToken currentLineToken = lineTokenServiceV2.getLineToken();
        lineTokenServiceV2.refreshNewToken();
        final LineToken newLineToken = lineTokenServiceV2.getLineToken();
        Assert.assertNotEquals(currentLineToken.getAccessToken(), newLineToken.getAccessToken());
    }

}
