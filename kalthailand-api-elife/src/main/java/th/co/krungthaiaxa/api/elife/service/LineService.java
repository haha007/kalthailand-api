package th.co.krungthaiaxa.api.elife.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.exception.LinePaymentException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.Charset.forName;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;

@Service
public class LineService {
    private final static Logger logger = LoggerFactory.getLogger(LineService.class);
    public static final String RESPONSE_CODE_ERROR_INTERNAL_LINEPAY = "9000";
    public static final String RESPONSE_CODE_SUCCESS = "0000";
    @Value("${line.pay.id}")
    private String linePayId;
    @Value("${line.pay.secret.key}")
    private String linePaySecretKey;
    @Value("${line.pay.url}")
    private String linePayUrl;
    @Value("${line.pay.capture}")
    private String linePayCapture;
    @Value("${line.app.id}")
    private String lineAppId;
    @Value("${line.app.notification.url}")
    private String lineAppNotificationUrl;
    private final LineTokenService lineTokenService;

    @Inject
    public LineService(LineTokenService lineTokenService) {
        this.lineTokenService = lineTokenService;
    }

    public void sendPushNotificationOld(String messageContent, String... mids) throws IOException {
        try {
            logger.info("Sending POST to LINE Push Notification Message");
            URL url = new URL(lineAppNotificationUrl);

            //set object header

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Line-ChannelToken", lineTokenService.getLineToken().getAccessToken());
            conn.setDoOutput(true);
            ObjectMapper mapper = new ObjectMapper();

            //parameter

            Map<String, Object> content = new HashMap<>();
            content.put("contentType", 1);
            content.put("toType", 1);
            content.put("text", messageContent);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("to", mids);
            data.put("toChannel", 1383378250);
            data.put("eventType", "138311608800106203");
            data.put("content", content);

            //set object to get response

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            mapper.writeValue(wr, data);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            logger.info("%nSending 'POST' request to URL : " + url);
            logger.info("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.info(response.toString());
            logger.info("Notification is sent with success");
        } catch (MalformedURLException e) {
            throw new IOException("Unable to send Line push notification.", e);
        } catch (IOException e) {
            throw new IOException("Unexpected to send Line push notification.", e);
        }
    }

    public void sendPushNotification(String messageContent, String... mids) throws IOException {
        if (StringUtils.isEmpty(lineAppNotificationUrl)) {
            logger.info("Notification is not configured and won't be sent");
            return;
        }

        LinePushNotificationContentRequest linePushNotificationContentRequest = new LinePushNotificationContentRequest();
        linePushNotificationContentRequest.setContentType(1);
        linePushNotificationContentRequest.setToType(1);
        linePushNotificationContentRequest.setText(messageContent);

        LinePushNotificationRequest linePushNotificationRequest = new LinePushNotificationRequest();
        linePushNotificationRequest.setToChannel(1383378250);
        linePushNotificationRequest.setEventType("138311608800106203");
        for (String mid : mids) {
            linePushNotificationRequest.addTo(mid);
        }
        linePushNotificationRequest.setContent(linePushNotificationContentRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Line-ChannelToken", lineTokenService.getLineToken().getAccessToken());
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(getJson(linePushNotificationRequest), forName("UTF-8")), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(lineAppNotificationUrl);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        } catch (Exception e) {
            throw new IOException("Unable to send push notification: " + e.getMessage(), e);
        }
        if (!response.getStatusCode().equals(OK)) {
            throw new IOException("Line's response for push notification is [" + response.getStatusCode() + "]. Response body is [" + response.getBody() + "]");
        }
        logger.info("Notification is sent with success");
    }

    public LinePayResponse bookPayment(String mid, Policy policy, String amount, String currency) throws IOException {
        logger.info("Booking payment");
        LinePayBookingRequest linePayBookingRequest = new LinePayBookingRequest();
        //TODO Product name here is the product display name. Should never use it. Should use productId.
        linePayBookingRequest.setProductName(policy.getCommonData().getProductId());
        linePayBookingRequest.setAmount(amount);
        linePayBookingRequest.setCurrency(currency);
        linePayBookingRequest.setCapture(linePayCapture);
        linePayBookingRequest.setMid(mid);
        linePayBookingRequest.setPayType("PREAPPROVED");
        linePayBookingRequest.setCheckConfirmUrlBrowser("true");
        linePayBookingRequest.setOrderId(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()) + "-" + policy.getPolicyId());
        linePayBookingRequest.setConfirmUrl("https://line.me/R/ch/" + lineAppId + "/elife/th/confirm/" + policy.getQuoteId() + "/" + policy.getPolicyId());
        linePayBookingRequest.setCancelUrl("https://line.me/R/ch/" + lineAppId + "/elife/th/paymentFailed/" + policy.getQuoteId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(getJson(linePayBookingRequest), forName("UTF-8")), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/request");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        } catch (Exception e) {
            throw new IOException("Unable to book payment: " + e.getMessage(), e);
        }
        if (!response.getStatusCode().equals(OK)) {
            throw new IOException("Line's response for booking payment is [" + response.getStatusCode() + "]. Response body is [" + response.getBody() + "]");
        }

        logger.info("Payment is booked with success");
        return getBookingResponseFromJSon(response.getBody());
    }

    public LinePayResponse confirmPayment(String transactionId, Double amount, String currency) throws IOException {
        logger.info("Confirming payment");
        LinePayConfirmingRequest linePayConfirmingRequest = new LinePayConfirmingRequest();
        linePayConfirmingRequest.setAmount(amount.toString());
        linePayConfirmingRequest.setCurrency(currency);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(getJson(linePayConfirmingRequest), forName("UTF-8")), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/" + transactionId + "/confirm");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        } catch (Exception e) {
            throw new IOException("Unable to confirm payment:" + e.getMessage(), e);
        }
        if (!response.getStatusCode().equals(OK)) {
            throw new IOException("Line's response for confirming payment is [" + response.getStatusCode() + "]. Response body is [" + response.getBody() + "]");
        }

        logger.info("Payment is confirmed with success");
        return getBookingResponseFromJSon(response.getBody());
    }

    public LinePayRecurringResponse preApproved(String regKey, Double amount, String currency, String productName, String orderId) throws IOException {
        LinePayRecurringResponse linePayResponse;

        try {
            logger.debug("Start sending POST to LINE Pay for Recurring Payment --------------------------------------->");
            URL url = new URL(linePayUrl + "/preapprovedPay/" + regKey + "/payment");
            //System.out.println("check url : " +url.toString());

            //set object header

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-LINE-ChannelId", linePayId);
            conn.setRequestProperty("X-LINE-ChannelSecret", linePaySecretKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            ObjectMapper mapper = new ObjectMapper();
            //System.out.println("check header : " +conn.getRequestProperties().toString());

            //parameter
            Map<String, Object> data = new HashMap<>();
            data.put("productName", productName);
            data.put("amount", amount);
            data.put("currency", currency);
            data.put("orderId", orderId);

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
            logger.info("check response : " + response.toString());
            logger.info("Notification is sent with success");
            linePayResponse = JsonUtil.mapper.readValue(response.toString(), LinePayRecurringResponse.class);

        } catch (Exception e) {
            throw new IOException("Error with preApproved: " + e.getMessage(), e);
        }
        System.out.println("Stop sending POST to LINE Pay for Recurring Payment --------------------------------------->");

        return linePayResponse;
    }

    public LinePayResponse preApprovedOld(String regKey, Double amount, String currency, String productName, String orderId) throws IOException {
        logger.info("preApproved payment");
        LinePayPreApprovedRequest linePayPreApprovedRequest = new LinePayPreApprovedRequest();
        linePayPreApprovedRequest.setProductName(productName);
        linePayPreApprovedRequest.setAmount(amount);
        linePayPreApprovedRequest.setCurrency(currency);
        linePayPreApprovedRequest.setOrderId(orderId);
        logger.info("linePayPreApprovedRequest : " + linePayPreApprovedRequest.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");
        logger.info("headers : " + headers.toString());

        HttpEntity<String> entity = new HttpEntity<>(new String(getJson(linePayPreApprovedRequest), forName("UTF-8")), headers);

        logger.info("entity.header : " + entity.getHeaders());
        logger.info("entity.body : " + entity.getBody());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/preapprovedPay/" + regKey + "/payment");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        } catch (RuntimeException e) {
            throw new IOException("Unable to preApproved payment", e);
        }
        if (!response.getStatusCode().equals(OK)) {
            throw new IOException("Line's response for preApproved payment is [" + response.getStatusCode() + "]. Response body is [" + response.getBody() + "]");
        }

        logger.info("Payment is preApproved with success");
        logger.info("response : " + response.getBody());
        LinePayResponse linePayResponse = getBookingResponseFromJSon(response.getBody());
        logger.info("Line Pay response has been read");

        return linePayResponse;
    }

    public LinePayResponse capturePayment(String transactionId, Double amount, String currency) {
        logger.info("Capturing payment");
        LinePayConfirmingRequest linePayConfirmingRequest = new LinePayConfirmingRequest();
        linePayConfirmingRequest.setAmount(amount.toString());
        linePayConfirmingRequest.setCurrency(currency);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(getJson(linePayConfirmingRequest), forName("UTF-8")), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/authorizations/" + transactionId + "/capture");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        } catch (RuntimeException e) {
            throw new LinePaymentException("Unable to capture payment: " + e.getMessage(), e);
        }
        if (!response.getStatusCode().equals(OK)) {
            throw new LinePaymentException("Line's response for capturing payment is [" + response.getStatusCode() + "]. Response body is [" + response.getBody() + "]");
        }

        logger.info("Payment is captured with success");
        LinePayResponse linePayResponse = getBookingResponseFromJSon(response.getBody());
        logger.info("Line Pay response has been read");

        return linePayResponse;
    }

    private LinePayResponse getBookingResponseFromJSon(String json) {
        return ObjectMapperUtil.toObject(JsonUtil.mapper, json, LinePayResponse.class);
    }

    private class LinePushNotificationRequest {
        private List<String> to = new ArrayList<>();
        private Integer toChannel;
        private String eventType;
        private LinePushNotificationContentRequest content;

        public List<String> getTo() {
            return to;
        }

        public void addTo(String to) {
            this.to.add(to);
        }

        public Integer getToChannel() {
            return toChannel;
        }

        public void setToChannel(Integer toChannel) {
            this.toChannel = toChannel;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public LinePushNotificationContentRequest getContent() {
            return content;
        }

        public void setContent(LinePushNotificationContentRequest content) {
            this.content = content;
        }
    }

    private class LinePushNotificationContentRequest {
        private Integer contentType;
        private Integer toType;
        private String text;

        public Integer getContentType() {
            return contentType;
        }

        public void setContentType(Integer contentType) {
            this.contentType = contentType;
        }

        public Integer getToType() {
            return toType;
        }

        public void setToType(Integer toType) {
            this.toType = toType;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private class LinePayBookingRequest {
        private String returnCode;
        private String returnMessage;
        private String productName;
        private String amount;
        private String currency;
        private String capture;
        private String mid;
        private String payType;
        private String checkConfirmUrlBrowser;
        private String orderId;
        private String confirmUrl;
        private String cancelUrl;

        public String getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMessage() {
            return returnMessage;
        }

        public void setReturnMessage(String returnMessage) {
            this.returnMessage = returnMessage;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getCapture() {
            return capture;
        }

        public void setCapture(String capture) {
            this.capture = capture;
        }

        public String getMid() {
            return mid;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }

        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

        public String getCheckConfirmUrlBrowser() {
            return checkConfirmUrlBrowser;
        }

        public void setCheckConfirmUrlBrowser(String checkConfirmUrlBrowser) {
            this.checkConfirmUrlBrowser = checkConfirmUrlBrowser;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getConfirmUrl() {
            return confirmUrl;
        }

        public void setConfirmUrl(String confirmUrl) {
            this.confirmUrl = confirmUrl;
        }

        public String getCancelUrl() {
            return cancelUrl;
        }

        public void setCancelUrl(String cancelUrl) {
            this.cancelUrl = cancelUrl;
        }
    }

    private class LinePayPreApprovedRequest {
        private String productName;
        private Double amount;
        private String currency;
        private String orderId;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        @Override
        public String toString() {
            return "LinePayPreApprovedRequest [productName=" + productName + ", amount=" + amount + ", currency="
                    + currency + ", orderId=" + orderId + "]";
        }

    }

    private class LinePayConfirmingRequest {
        private String amount;
        private String currency;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}