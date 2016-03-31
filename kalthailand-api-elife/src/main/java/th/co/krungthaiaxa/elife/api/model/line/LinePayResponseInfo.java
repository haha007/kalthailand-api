package th.co.krungthaiaxa.elife.api.model.line;

public class LinePayResponseInfo {
    private String orderId;
    private String transactionId;
    private String paymentAccessToken;
    private String regKey;
    private LinePayResponsePayment paymentUrl = new LinePayResponsePayment();
    private LinePayResponsePaymentInfo payInfo = new LinePayResponsePaymentInfo();

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

    public LinePayResponsePaymentInfo getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(LinePayResponsePaymentInfo payInfo) {
        this.payInfo = payInfo;
    }
}
