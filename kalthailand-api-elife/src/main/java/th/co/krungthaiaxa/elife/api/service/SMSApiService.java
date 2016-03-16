package th.co.krungthaiaxa.elife.api.service;

import org.apache.http.HttpEntity;
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
import th.co.krungthaiaxa.elife.api.model.Policy;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SantiLik on 3/16/2016.
 */
@Service
public class SMSApiService {

    private final static Logger logger = LoggerFactory.getLogger(SMSApiService.class);
    @Value("${sms.config.url}")
    private String smsUrl;
    @Value("${sms.config.user}")
    private String smsUser;
    @Value("${sms.config.pass}")
    private String smsPass;

    public Map<String,String> sendConfirmationMessage(Policy pol) throws IOException {
        logger.info(String.format("[%1$s] ...","sendConfirmationMessage"));
        logger.info(String.format("policy id is %1$s, mobile number is %2$s",pol.getPolicyId(),pol.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber()));

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(smsUrl);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("ACCOUNT", smsUser));
        params.add(new BasicNameValuePair("PASSWORD", smsPass));
        params.add(new BasicNameValuePair("MOBILE", pol.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber()));
        params.add(new BasicNameValuePair("MESSAGE", "ขอบคุณที่สมัครประกันกับกรุงไทย-แอกซ่า (หมายเลขอ้างอิง: " + pol.getPolicyId() + ")" + System.getProperty("line.separator") + System.getProperty("line.separator") + "ข้อมูลเพิ่มเติมโทร 1159 หรือคลิก www.krungthai-axa.co.th"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "TIS-620"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String responseAsString = EntityUtils.toString(response.getEntity());
        String[] splitTest = responseAsString.split("\\n");
        Map<String,String> res = new HashMap<>();
        for(Integer a = 0; a < splitTest.length; a++){
            String[] keyValue = splitTest[a].split("=");
            res.put(keyValue[0],keyValue[1]);
        }

        //{STATUS=0, MESSAGE_ID=356976723, END=OK, TASK_ID=22777652}
        logger.info(res.toString());

        return res;
    }

}
