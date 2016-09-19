package th.co.krungthaiaxa.api.elife.data;

import java.time.LocalDateTime;

public class DeductionFileLine {
    private String policyNumber;
    private String bankCode;
    private String paymentMode;
    private String paymentId;
    private Double amount;
    private LocalDateTime processDate;
    private String rejectionCode;
    private String rejectionMessage;
    private String informCustomerCode;
    private String informCustomerMessage;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getProcessDate() {
        return processDate;
    }

    public void setProcessDate(LocalDateTime processDate) {
        this.processDate = processDate;
    }

    public String getRejectionCode() {
        return rejectionCode;
    }

    public void setRejectionCode(String rejectionCode) {
        this.rejectionCode = rejectionCode;
    }

    public String getRejectionMessage() {
        return rejectionMessage;
    }

    public void setRejectionMessage(String rejectionMessage) {
        this.rejectionMessage = rejectionMessage;
    }

    public String getInformCustomerCode() {
        return informCustomerCode;
    }

    public void setInformCustomerCode(String informCustomerCode) {
        this.informCustomerCode = informCustomerCode;
    }

    public String getInformCustomerMessage() {
        return informCustomerMessage;
    }

    public void setInformCustomerMessage(String informCustomerMessage) {
        this.informCustomerMessage = informCustomerMessage;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
