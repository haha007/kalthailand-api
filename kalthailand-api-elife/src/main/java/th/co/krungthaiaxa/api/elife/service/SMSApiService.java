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
import th.co.krungthaiaxa.api.elife.exception.SMSException;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SMSApiService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SMSApiService.class);
    @Value("${sms.config.url}")
    private String smsUrl;
    @Value("${sms.config.user}")
    private String smsUser;
    @Value("${sms.config.pass}")
    private String smsPass;

    /**
     * @param policy
     * @param message
     * @return
     * @throws IOException
     */
    @Deprecated
    public SMSResponse sendConfirmationMessage(Policy policy, String message) throws IOException {
        LOGGER.debug(String.format("Send SMS to main insured of policy %s", policy.getPolicyId()));

        Insured mainPerson = ProductUtils.validateExistMainInsured(policy);
        String phoneNumber = mainPerson.getPerson().getMobilePhoneNumber().getNumber();
        return sendMessageNoCatchException(phoneNumber, message);
    }

    /**
     * This method will never throw Exception.
     * The error code will be included in the result if there's something wrong.
     *
     * @param phoneNumber
     * @param message
     * @return
     */
    public SMSResponse sendMessageIgnoreFail(String phoneNumber, String message) {
        try {
            return sendMessageNoCatchException(phoneNumber, message);
        } catch (Exception ex) {
            LOGGER.error("Cannot send SMS Message: " + ex.getMessage(), ex);
            SMSResponse response = new SMSResponse();
            response.setStatus(SMSResponse.STATUS_INTERNAL_FAIL);
            return response;
        }
    }

    public SMSResponse sendMessage(String phoneNumber, String message) {
        try {
            return sendMessageNoCatchException(phoneNumber, message);
        } catch (IOException ex) {
            throw new SMSException("Cannot send SMS Message: " + ex.getMessage(), ex);
        }
    }

    public SMSResponse sendMessageNoCatchException(String phoneNumber, String message) throws IOException {
        LOGGER.debug(String.format("Send SMS to phone %s", phoneNumber));

        SMSResponse res = new SMSResponse();
        if (StringUtils.isBlank(smsUrl)) {
            res.setStatus(SMSResponse.STATUS_SUCCESS);
            return res;
        }

        message = message.substring(1, message.length());

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(smsUrl);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("ACCOUNT", smsUser));
        params.add(new BasicNameValuePair("PASSWORD", smsPass));
        params.add(new BasicNameValuePair("MOBILE", phoneNumber));
        params.add(new BasicNameValuePair("MESSAGE", message));
        httppost.setEntity(new UrlEncodedFormEntity(params, "TIS-620"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        String responseAsString = EntityUtils.toString(response.getEntity());
        LOGGER.debug("SMS response as string: " + responseAsString);
        return toSMSResponse(responseAsString);
    }

    /**
     * @param responseAsString {STATUS=0, MESSAGE_ID=356976723, END=OK, TASK_ID=22777652} = working properly
     *                         {STATUS=502, END=OK} = user name or password is wrong
     *                         {STATUS=504, END=OK} = credit is out
     * @return
     */
    private SMSResponse toSMSResponse(String responseAsString) {
        SMSResponse response = new SMSResponse();
        response.setResponseMessage(responseAsString);

        Map<String, String> responseMap = new HashMap<>();
        String[] splitTest = responseAsString.split("\\n");
        for (String aSplitTest : splitTest) {
            String[] keyValue = aSplitTest.split("=");
            String fieldName = keyValue[0];
            String fieldValue = keyValue[1];
            responseMap.put(fieldName, fieldValue);
        }

        convertSMSResponse(responseMap, response);
        return response;
    }

    private void convertSMSResponse(Map<String, String> source, SMSResponse destination) {
        destination.setEnd(source.get("END"));
        destination.setStatus(source.get("STATUS"));
        destination.setMessageId(source.get("MESSAGE_ID"));
        destination.setTaskId(source.get("TASK_ID"));
    }
}
