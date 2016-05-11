package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SMSApiService {

    private final static Logger logger = LoggerFactory.getLogger(SMSApiService.class);
    @Value("${sms.config.url}")
    private String smsUrl;
    @Value("${sms.config.user}")
    private String smsUser;
    @Value("${sms.config.pass}")
    private String smsPass;

    public Map<String, String> sendConfirmationMessage(Policy policy, String message) throws IOException {
        logger.info(String.format("[%1$s] ...", "sendConfirmationMessage"));
        logger.info(String.format("policy id is %1$s, mobile number is %2$s", policy.getPolicyId(), policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber()));

        Map<String, String> res = new HashMap<>();
        if (StringUtils.isBlank(smsUrl)) {
            res.put("STATUS", "0");
            return res;
        }

        message = message.substring(1, message.length());

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(smsUrl);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("ACCOUNT", smsUser));
        params.add(new BasicNameValuePair("PASSWORD", smsPass));
        params.add(new BasicNameValuePair("MOBILE", policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber()));
        params.add(new BasicNameValuePair("MESSAGE", message));
        httppost.setEntity(new UrlEncodedFormEntity(params, "TIS-620"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        String responseAsString = EntityUtils.toString(response.getEntity());
        String[] splitTest = responseAsString.split("\\n");
        for (String aSplitTest : splitTest) {
            String[] keyValue = aSplitTest.split("=");
            res.put(keyValue[0], keyValue[1]);
        }

        //{STATUS=0, MESSAGE_ID=356976723, END=OK, TASK_ID=22777652} = working properly
        //{STATUS=502, END=OK} = user name or password is wrong
        //{STATUS=504, END=OK} = credit is out
        logger.info(res.toString());

        return res;
    }

}
