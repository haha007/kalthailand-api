package th.co.krungthaiaxa.api.elife.line;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service(value = "LineTokenServiceV2")
public class LineTokenService {

    private final static Logger logger = LoggerFactory.getLogger(LineTokenService.class);
    private final LineTokenRepository lineTokenRepository;
    @Value("${line.app.secret.key}")
    private String secretKey;
    @Value("${line.app.reissue.url}")
    private String lineAppReIssueUrl;
    private final String DATE_FORMAT = "yyyy/MM/dd";

    @Inject
    public LineTokenService(LineTokenRepository lineTokenRepository) {
        this.lineTokenRepository = lineTokenRepository;
    }

    public LineToken getLineToken() {
        return lineTokenRepository.findByRowId(1);
    }

    public LineToken validateExistLineToken() {
        LineToken lineToken = getLineToken();
        if (lineToken == null) {
            throw new BaseException(ErrorCode.ERROR_CODE_LINE_TOKEN_NOT_EXIST, "Not found any line token.");
        }
        return lineToken;
    }

    public void updateLineToken(LineToken updateObject) {
        lineTokenRepository.deleteAll();
        updateObject.setRowId(1);
        lineTokenRepository.save(updateObject);
    }

    //@Scheduled(fixedRate = 432000000)
    public void refreshNewToken() {
        logger.info("Time Trigger to check and re-issue for line token ------->");
        try {
            reIssueLineToken();
        } catch (IOException e) {
            logger.error("Cannot refresh new line token: {}", e.getMessage(), e);
            //This is the cron job, we don't need to throw exception to UI.
        }
    }

    @SuppressWarnings("deprecation")
    private void reIssueLineToken() throws IOException {
        logger.info("Sending POST to LINE to get new token");

        LineToken oldToken = validateExistLineToken();
        //create url
        URL url = new URL(lineAppReIssueUrl + "?refreshToken=" + oldToken.getRefreshToken() + "&channelSecret=" + secretKey);

        //set object header
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("X-Line-ChannelToken", oldToken.getAccessToken());
        conn.setDoOutput(true);
        ObjectMapper mapper = new ObjectMapper();

        //parameter
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("to", "xxx");

        //set object to get response
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        mapper.writeValue(wr, data);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        //logger.info("%nSending 'POST' request to URL : " + url);
        //logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //get response as object
        ObjectMapper mm = new ObjectMapper();
        Map<String, Object> obj = mm.readValue(response.toString(), Map.class);
        LineToken newToken = new LineToken();
        newToken.setAccessToken((String) obj.get("accessToken"));
        newToken.setRefreshToken((String) obj.get("refreshToken"));
        newToken.setExpireDate((new SimpleDateFormat("yyyy/MM/dd")).format(new Date((Long) obj.get("expire"))));

        //update in db
        updateLineToken(newToken);

        logger.info("re-issue token is refresh with success ------->");

    }

}
