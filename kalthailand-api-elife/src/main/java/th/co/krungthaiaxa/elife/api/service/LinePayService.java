package th.co.krungthaiaxa.elife.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.line.LinePayResponse;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.http.HttpMethod.POST;

@Service
public class LinePayService {
    private final static Logger logger = LoggerFactory.getLogger(LinePayService.class);
    public static final String LINE_PAY_INTERNAL_ERROR = "9000";
    @Value("${line.pay.id}")
    private String linePayId;
    @Value("${line.pay.secret.key}")
    private String linePaySecretKey;
    @Value("${line.pay.url}")
    private String linePayUrl;
    @Value("${line.app.id}")
    private String lineAppId;

    public LinePayResponse bookPayment(String mid, Policy policy, String amount, String currency) throws IOException {
        logger.info("Booking payment");
        RestTemplate restTemplate = new RestTemplate();
        LinePayBookingRequest linePayBookingRequest = new LinePayBookingRequest();
        linePayBookingRequest.setProductName(policy.getCommonData().getProductName());
        linePayBookingRequest.setAmount(amount);
        linePayBookingRequest.setCurrency(currency);
        linePayBookingRequest.setCapture("false");
        linePayBookingRequest.setMid(mid);
        linePayBookingRequest.setPayType("PREAPPROVED");
        linePayBookingRequest.setCheckConfirmUrlBrowser("true");
        linePayBookingRequest.setOrderId(DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss").format(LocalDateTime.now()) + "-" + policy.getPolicyId());
        linePayBookingRequest.setConfirmUrl("https://line.me/R/ch/" + lineAppId + "/elife/th/confirm/" + policy.getQuoteId() + "/" + policy.getPolicyId());
        linePayBookingRequest.setCancelUrl("https://line.me/R/ch/" + lineAppId + "/elife/th/paymentFailed/" + policy.getQuoteId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(JsonUtil.getJson(linePayBookingRequest)), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/request");
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        return getBookingResponseFromJSon(response.getBody());
    }

    public LinePayResponse confirmPayment(String transactionId, Double amount, String currency) throws IOException {
        logger.info("Confirming payment");
        RestTemplate restTemplate = new RestTemplate();
        LinePayConfirmingRequest linePayConfirmingRequest = new LinePayConfirmingRequest();
        linePayConfirmingRequest.setAmount(amount.toString());
        linePayConfirmingRequest.setCurrency(currency);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-LINE-ChannelId", linePayId);
        headers.set("X-LINE-ChannelSecret", linePaySecretKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(new String(JsonUtil.getJson(linePayConfirmingRequest)), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linePayUrl + "/" + transactionId + "/confirm");
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), POST, entity, String.class);
        return getBookingResponseFromJSon(response.getBody());
    }

    private LinePayResponse getBookingResponseFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, LinePayResponse.class);
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