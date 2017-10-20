package th.co.krungthaiaxa.api.elife.line.v2.service;

import okhttp3.ResponseBody;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.v2.client.LineAPI;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.LineMessage;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.LineMultiCastMessage;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.MessageObject;
import th.co.krungthaiaxa.api.elife.line.v2.http.Client;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineAccessToken;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @author tuong.le on 10/19/17.
 */
@Service
public class LineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LineService.class);
    private static final String GRANT_TYPE_CLIENT_CODE = "client_credentials";
    private static final String DATE_FORMAT = "yyyy/MM/dd";

    @Value("${line.v2.app.client.id}")
    private String channelId;
    @Value("${line.v2.app.client.secret}")
    private String channelSecret;
    @Value("${line.v2.app.reissue.url}")
    private String lineAppReIssueUrl;

    private final LineTokenRepository lineTokenRepository;

    @Inject
    public LineService(LineTokenRepository lineTokenRepository) {
        this.lineTokenRepository = lineTokenRepository;
    }

    @Scheduled(fixedRate = 432000000)
    public void cronRefreshNewToken() {
        LOGGER.info("Time Trigger to check and re-issue for line token ------->");
        try {
            reIssueLineToken();
        } catch (IOException e) {
            LOGGER.error("Cannot refresh new line token: {}", e.getMessage(), e);
        }
    }

    private void reIssueLineToken() throws IOException {
        LOGGER.info("Sending POST to LINE to get new token");
        try {
            final LineAccessToken token = getClient(t -> t.accessToken(
                    GRANT_TYPE_CLIENT_CODE,
                    channelId,
                    channelSecret));
            LineToken tokenEntity = new LineToken();
            tokenEntity.setAccessToken(token.getAccessToken());
            tokenEntity.setRefreshToken("");
            tokenEntity.setExpireDate((new SimpleDateFormat(DATE_FORMAT))
                    .format(new Date(token.getExpiresIn() * 1000L + System.currentTimeMillis())));

            updateLineToken(tokenEntity);
            LOGGER.info("re-issue token is refresh with success ------->");
        } catch (RuntimeException ex) {
            LOGGER.error("Could not reIssueLineToken: ", ex);
        }
    }

    private void updateLineToken(LineToken updateObject) {
        lineTokenRepository.deleteAll();
        updateObject.setRowId(1);
        lineTokenRepository.save(updateObject);
    }

    public boolean pushTextMessage(final String to, final String text) {
        MessageObject messageObject = new MessageObject();
        messageObject.setType("text");
        messageObject.setText(text);

        LineMessage lineMessage = new LineMessage();
        lineMessage.setTo(to);
        lineMessage.setMessages(messageObject);
        try {
            // LINE return 200 status and empty JSON object
            return !Objects.isNull(getClient(t -> t.pushMessage(
                    this.getAccessToken(), lineMessage)));
        } catch (RuntimeException ex) {
            LOGGER.error("Could not reIssueLineToken: ", ex);
            return false;
        }
    }

    public boolean pushMulticastMessage(final Set<String> to, final List<MessageObject> messages) {
        LineMultiCastMessage multicastMessage = new LineMultiCastMessage();
        multicastMessage.setTo(to);
        multicastMessage.setMessages(messages);
        try {
            // LINE return 200 status and empty JSON object
            return !Objects.isNull(getClient(t -> t.pushMulticastMessage(
                    this.getAccessToken(), multicastMessage)));
        } catch (RuntimeException ex) {
            LOGGER.error("Could not reIssueLineToken: ", ex);
            return false;
        }
    }

    public String getLineUserIdFromMid(final String mid) {
        try {
            final ResponseBody res = getClient(t -> t.getLineUserIdFromMid(this.getAccessToken(), mid));
            return Objects.isNull(res) ? StringUtils.EMPTY : res.string();
        } catch (IOException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    private <R> R getClient(final Function<LineAPI, Call<R>> function) {
        return Client.getClient("https://api.line.me/", LineAPI.class, function);
    }

    private String getAccessToken() {
        try {
            return "Bearer " + lineTokenRepository.findByRowId(1).getAccessToken();
        } catch (Exception e) {
            throw e;
        }
    }
}
