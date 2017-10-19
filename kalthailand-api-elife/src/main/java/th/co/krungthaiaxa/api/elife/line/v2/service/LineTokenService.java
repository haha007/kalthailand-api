package th.co.krungthaiaxa.api.elife.line.v2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineAccessToken;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class LineTokenService {

    private final static Logger LOGGER = LoggerFactory.getLogger(LineTokenService.class);
    private final LineTokenRepository lineTokenRepository;

    @Value("${line.v2.app.client.id}")
    private String clientId;
    @Value("${line.v2.app.client.secret}")
    private String clientSecret;
    @Value("${line.v2.app.reissue.url}")
    private String lineAppReIssueUrl;
    private final String DATE_FORMAT = "yyyy/MM/dd";

    private RestTemplate restTemplate = new RestTemplate();

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

    @Scheduled(fixedRate = 432000000)
    public void refreshNewToken() {
        LOGGER.info("Time Trigger to check and re-issue for line token ------->");
        try {
            reIssueLineToken();
        } catch (IOException e) {
            LOGGER.error("Cannot refresh new line token: {}", e.getMessage(), e);
            //This is the cron job, we don't need to throw exception to UI.
        }
    }

    private void reIssueLineToken() throws IOException {
        LOGGER.info("Sending POST to LINE to get new token");

        /*RequestLineAccessToken requestToken = new RequestLineAccessToken();
        requestToken.setGrantType("client_credentials");
        requestToken.setClientId(clientId);
        requestToken.setClientSecret(clientSecret);*/
        /*MultiValueMap<String, Object> variablesMap = new LinkedMultiValueMap<String, Object>();
        variablesMap.add("grant_type", "client_credentials");
        variablesMap.add("client_id", clientId);
        variablesMap.add("client_secret", clientSecret);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final HttpEntity<String> requestWrapper =
                new HttpEntity<>(new String(JsonUtil.getJson(variablesMap)), headers);

        final Optional<LineAccessToken> newLineToken = this.requestClient(lineAppReIssueUrl, requestWrapper, POST);
        newLineToken.ifPresent(lineAccessToken -> {
            LineToken lineTokenEntity = new LineToken();
            lineTokenEntity.setAccessToken(lineAccessToken.getAccessToken());
            lineTokenEntity.setRefreshToken("");
            final Long expiresInSec = lineAccessToken.getExpiresIn();
            lineTokenEntity.setExpireDate((new SimpleDateFormat("yyyy/MM/dd"))
                    .format(new Date(expiresInSec + System.currentTimeMillis() / 1000L)));

            //update in db
            updateLineToken(lineTokenEntity);
        });*/
        
        
        
        
        
        
        
        LineToken oldToken = validateExistLineToken();
        //create url
        URL url = new URL(lineAppReIssueUrl);

        //set object header
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        ObjectMapper mapper = new ObjectMapper();

        //parameter
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("grant_type", "client_credentials");
        data.put("client_id", clientId);
        data.put("client_secret", clientSecret);


        //set object to get response
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        mapper.writeValue(wr, data);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        //LOGGER.info("%nSending 'POST' request to URL : " + url);
        //LOGGER.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //get response as object
        ObjectMapper mm = new ObjectMapper();
        Map obj = mm.readValue(response.toString(), Map.class);
        LineToken newToken = new LineToken();
        newToken.setAccessToken((String) obj.get("access_token"));
        newToken.setRefreshToken("");
        final Long expiresInSec = (Long) obj.get("expires_in");
        if (Objects.isNull(expiresInSec)) {
            LOGGER.error("Could not reissue Line Token: expires_in is empty");
            return;
        }
        newToken.setExpireDate((new SimpleDateFormat("yyyy/MM/dd")).format(new Date(expiresInSec + System.currentTimeMillis() / 1000L)));

        //update in db
        updateLineToken(newToken);

        LOGGER.info("re-issue token is refresh with success ------->");

    }

    private Optional<LineAccessToken> requestClient(final String url,
                                                    final HttpEntity<String> request,
                                                    final HttpMethod method) {
        try {
            //SSLUtils.disableSslVerification();
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            final ResponseEntity<LineAccessToken> response =
                    restTemplate.exchange(builder.toUriString(), method, request, LineAccessToken.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) && !Objects.isNull(response.getBody())) {
                return Optional.of(response.getBody());
            }
        } catch (Exception exception) {
            LOGGER.error("Could not Handle the response exception: ", exception);
        }
        return Optional.empty();

    }

}
