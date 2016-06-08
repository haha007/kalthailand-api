package th.co.krungthaiaxa.api.elife.service;

import static java.nio.charset.Charset.forName;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

@Service
public class LineTokenService {
	
	private final static Logger logger = LoggerFactory.getLogger(LineTokenService.class);
	private final LineTokenRepository lineTokenRepository;
	@Value("${line.app.secret.key}")
    private String secretKey;
    @Value("${line.app.reissue.url}")
    private String lineAppReIssueUrl;
    private final String DATE_FORMAT = "yyyy/MM/dd";
	
	@Inject
	public LineTokenService(LineTokenRepository lineTokenRepository){
		this.lineTokenRepository = lineTokenRepository;
	}
	
	public LineToken getLineToken(){
		return lineTokenRepository.findByRowId(1);
	}
	
	public void updateLineToken(LineToken updateObject){
		lineTokenRepository.deleteAll();
		updateObject.setRowId(1);
		lineTokenRepository.save(updateObject);		
	}
	
	@Scheduled(fixedRate=432000000)
	public void refreshNewToken(){
		logger.info("Time Trigger to check and re-issue for line token ------->");
		try {
			reIssueLineToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	@SuppressWarnings("deprecation")
	private void reIssueLineToken() throws IOException {		
		logger.info("Sending POST to LINE to get new token");
		
		LineToken oldToken = getLineToken();
		try {   
			//create url
            URL url = new URL(lineAppReIssueUrl + "?refreshToken="+oldToken.getRefreshToken()+"&channelSecret="+secretKey);

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
            //logger.info("\nSending 'POST' request to URL : " + url);
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
            Map<String,Object> obj = mm.readValue(response.toString(), Map.class );
            LineToken newToken = new LineToken();
            newToken.setAccessToken((String)obj.get("accessToken"));
            newToken.setRefreshToken((String)obj.get("refreshToken"));
            newToken.setExpireDate((new SimpleDateFormat("yyyy/MM/dd")).format(new Date((Long)obj.get("expire"))));
            
            //update in db
            updateLineToken(newToken);
            
            logger.info("re-issue token is refresh with success ------->");
        } catch (MalformedURLException e) {
            throw new IOException("Unable to send Line push notification.", e);
        } catch (IOException e) {
            throw new IOException("Unexpected to send Line push notification.", e);
        }
		
    }

	
}
