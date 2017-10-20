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
import th.co.krungthaiaxa.api.elife.line.v2.client.model.MessageObject;
import th.co.krungthaiaxa.api.elife.line.v2.service.LineService;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public void should_send_multicast_messages_to_user() {
        final Set<String> to = Collections.singleton("Ueed6d8caf72f3a4cac40dc84e04f1643");
        final MessageObject messageObject = new MessageObject();
        messageObject.setText("TEsting");
        messageObject.setType("text");
        final List<MessageObject> messages = Collections.singletonList(messageObject);
        Assert.assertTrue(lineService.pushMulticastMessage(to, messages));
    }

    @Test
    public void could_not_send_multicast_messages_to_incorrect_user_ID_user() {
        final Set<String> to = Collections.singleton("f72f3a4cac40dc84e04f1643");
        final MessageObject messageObject = new MessageObject();
        messageObject.setText("TEsting");
        messageObject.setType("text");
        final List<MessageObject> messages = Collections.singletonList(messageObject);
        Assert.assertFalse(lineService.pushMulticastMessage(to, messages));
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

}
