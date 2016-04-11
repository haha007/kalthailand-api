package th.co.krungthaiaxa.elife.api.model.line;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LinePayResponseInfo {
    private String orderId;
    private String transactionId;
    private String paymentAccessToken;
    private String regKey;
    private LinePayResponsePayment paymentUrl = new LinePayResponsePayment();
    private List<LinePayResponsePaymentInfo> payInfo = new ArrayList<>();
    private LocalDate authorizationExpireDate;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentAccessToken() {
        return paymentAccessToken;
    }

    public void setPaymentAccessToken(String paymentAccessToken) {
        this.paymentAccessToken = paymentAccessToken;
    }

    public String getRegKey() {
        return regKey;
    }

    public void setRegKey(String regKey) {
        this.regKey = regKey;
    }

    public LinePayResponsePayment getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(LinePayResponsePayment paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public List<LinePayResponsePaymentInfo> getPayInfo() {
        return payInfo;
    }

    public void addPayInfo(LinePayResponsePaymentInfo payInfo) {
        this.payInfo.add(payInfo);
    }

    public LocalDate getAuthorizationExpireDate() {
        return authorizationExpireDate;
    }

    public void setAuthorizationExpireDate(LocalDate authorizationExpireDate) {
        this.authorizationExpireDate = authorizationExpireDate;
    }
}
