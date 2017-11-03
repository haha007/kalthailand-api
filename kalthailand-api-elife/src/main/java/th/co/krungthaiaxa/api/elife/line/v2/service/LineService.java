package th.co.krungthaiaxa.api.elife.line.v2.service;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import th.co.krungthaiaxa.api.common.client.Client;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.v2.client.LineAPI;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.LineMultiCastMessage;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.MessageObject;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineAccessToken;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineIdMap;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        LineMultiCastMessage lineMessage = new LineMultiCastMessage();
        lineMessage.setTo(Collections.singleton(to));
        lineMessage.setMessages(Collections.singletonList(messageObject));
        try {
            // LINE return 200 status and empty JSON object
            return !Objects.isNull(getClient(t -> t.pushMulticastMessage(
                    this.getAccessToken(), lineMessage)));
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
            LOGGER.error("Could not convert MID {} to LINE userId: ", e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    public List getMultiplierLineUserIdFromMid(final Set<String> mids) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), mids.stream().collect(Collectors.joining("\n")));
        try {
            final ResponseBody midWithUserIdListRes =
                    getClient(t -> t.getMultiplierLineUserIdFromMid(this.getAccessToken(), body));
            if (Objects.isNull(midWithUserIdListRes)) {
                return Collections.EMPTY_LIST;
            }

            return Arrays.stream(midWithUserIdListRes.string().split("\n"))
                    .map(line -> {
                        final String[] map = line.split(" ");
                        return new LineIdMap(map[0], map[1]);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * get userId (Line V2): if insured have Line userId return it, otherwise convert mid to userId then return it
     *
     * @param insured insured
     * @return LINE userId as String
     */
    public String getLineUserIdFromInsure(final Insured insured) {
        final String lineUserId = insured.getPerson().getLineUserId();
        if (StringUtils.isNotBlank(lineUserId)) {
            return lineUserId;
        } else {
            final String mid = insured.getPerson().getLineId();
            LOGGER.warn("Need to convert MID {} to LINE userId", mid);
            return this.getLineUserIdFromMid(mid);
        }
    }

    private <R> R getClient(final Function<LineAPI, Call<R>> function) {
        return Client.getClient("https://api.line.me/", LineAPI.class, function);
    }

    private String getAccessToken() {
        return "Bearer " + lineTokenRepository.findByRowId(1).getAccessToken();
    }
}
