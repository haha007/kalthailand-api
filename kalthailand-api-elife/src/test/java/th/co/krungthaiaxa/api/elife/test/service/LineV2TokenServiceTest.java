package th.co.krungthaiaxa.api.elife.test.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineIdMap;
import th.co.krungthaiaxa.api.elife.line.v2.service.LineService;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class LineV2TokenServiceTest {

    @Inject
    private LineTokenRepository lineTokenRepository;

    @Inject
    private LineService lineService;

    @Test
    public void should_get_access_token_from_line_cron_job() {
        final LineToken currentLineToken = lineTokenRepository.findByRowId(1);
        lineService.cronRefreshNewToken();
        final LineToken newLineToken = lineTokenRepository.findByRowId(1);
        Assert.assertNotEquals(currentLineToken.getAccessToken(), newLineToken.getAccessToken());
    }

    @Test
    public void should_push_messages_to_user() {
        final String to = "Uc1a4308943983be0a916166436b365f5";
        final String messageText = "TEsting";
        Assert.assertTrue(lineService.pushTextMessage(to, messageText));
    }

    @Test
    public void could_not_push_messages_to_invalid_user() {
        final String to = "722176490b7a3ee8c6ec820e8c044";
        final String messageText = "TEsting";
        Assert.assertFalse(lineService.pushTextMessage(to, messageText));
    }

    @Test
    public void could_convert_mid_to_user_ID_user() {
        final String mid = "uf039a904e629a46d7c9d1b82b97de3c8";
        final String userId = lineService.getLineUserIdFromMid(mid);
        Assert.assertTrue(StringUtils.isNotEmpty(userId));
        Assert.assertNotSame(mid, userId);
    }

    @Test
    public void could_not_convert_invalid_mid_to_user_ID_user() {
        final String mid = "ua46d7c9d1b82b97de3c8";
        
        final String userId = lineService.getLineUserIdFromMid(mid);
        Assert.assertTrue(StringUtils.isEmpty(userId));
    }

    @Test
    public void could_convert_multiplier_mids_to_user_ID_user() {
        final String mids = "uf039a904e629a46d7c9d1b82b97de3c8";
        final List<LineIdMap> userIdMaps = lineService.getMultiplierLineUserIdFromMid(Collections.singleton(mids));
        Assert.assertFalse(Objects.isNull(userIdMaps));
    }

}
