package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jsoup.helper.StringUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//import org.springframework.data.mongodb.core.mapping.Document;

/**
 * If a payment session is fail for any reason, the status will be {@link PaymentStatus#INCOMPLETE} or {@link PaymentStatus#OVERPAID}.
 * Then if payment is processed in the next time, it will create another payment object.
 */
@ApiModel(description = "Data concerning the payment")
@org.springframework.data.mongodb.core.mapping.Document(collection = "payment")
public class Payment implements Serializable {

    public static final int REGISTRATION_KEY_PLAIN_TEXT_MAX_LENGTH = 100;
    @Id
    private String paymentId;
    @ApiModelProperty(value = "If this payment is fail, the user will retry with new payment. That retry paymentId will be stored in this field.")
    @Indexed
    private String retryPaymentId;
    @ApiModelProperty(value = "If this payment is the retry from an original payment, this value is true.")
    private Boolean retried;
    private String policyId;
    private String orderId;
    private String transactionId;
    private String registrationKey;
    private PaymentStatus status;
    private LocalDateTime dueDate;
    /**
     * This field only have value after checking with LineService and get response (can fail or not)!
     */
    private LocalDateTime effectiveDate;
    private Amount amount;
    @ApiModelProperty(value = "List of payment status done for the specific payment. If not empty, " +
            "should contain 0 to N payments with status SUCCESS for which the sum of the amounts is equal to " +
            "expected amount. May contain 0 to N unsuccessful payments.")
    private List<PaymentInformation> paymentInformations = new ArrayList<>();
    @DBRef
    private Document receiptImageDocument;
    @DBRef
    private Document receiptPdfDocument;

    @Indexed
    private String receiptFullNumberBase36;
//    @Indexed
//    private Long receiptFullNumber;

    @Indexed
    private Long receiptMainNumber;
    @Indexed
    private String receiptMainNumberBase36;

    @Indexed
    private Boolean receiptNumberOldPattern;

    public Payment() {
        // Used by Jackson
    }

    public Payment(String policyId, Double value, String currencyCode, LocalDateTime dueDate) {
        this.policyId = policyId;
        this.status = PaymentStatus.NOT_PROCESSED;
        this.dueDate = dueDate;
        this.amount = new Amount();
        amount.setValue(value);
        amount.setCurrencyCode(currencyCode);
    }

    @ApiModelProperty(required = true, value = "The payment Id")
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @ApiModelProperty(required = true, value = "The id of the policy the payment refers to")
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @ApiModelProperty(required = true, value = "The order Id used to book the payment (if any). This is used by pay gateway like LINE Pay.")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @ApiModelProperty(required = true, value = "The transaction Id of the payment (if any). This is used by pay gateway like LINE Pay.")
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @ApiModelProperty(value = "The payment's registration key in case of recurring payment. This is used by pay gateway like LINE Pay.")
    public String getRegistrationKey() {
        String result = registrationKey;
        if (!StringUtil.isBlank(registrationKey) && isEncrypted(registrationKey)) {
            result = EncryptUtil.decrypt(registrationKey);
        }
        return result;
    }

    public void setRegistrationKey(String registrationKey) {
        String result = registrationKey;
        if (!StringUtil.isBlank(registrationKey) && !isEncrypted(registrationKey)) {
            result = EncryptUtil.encrypt(registrationKey);
        }
        this.registrationKey = result;
    }

    /**
     * This method is used only to compatible to old data (plaintext).
     *
     * @param registrationKey
     * @return
     */
    private boolean isEncrypted(String registrationKey) {
        return registrationKey.length() > REGISTRATION_KEY_PLAIN_TEXT_MAX_LENGTH;
    }

    @ApiModelProperty(required = true, value = "Status of the payment")
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @ApiModelProperty(required = true, value = "The payment due date. Can be in future.")
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    @ApiModelProperty(value = "The payment effective date. null if payment has not been done yet.")
    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @ApiModelProperty(required = true, value = "The payment's amount.")
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public List<PaymentInformation> getPaymentInformations() {
        return paymentInformations;
    }

    public void addPaymentInformation(PaymentInformation paymentInformation) {
        paymentInformations.add(paymentInformation);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getRetryPaymentId() {
        return retryPaymentId;
    }

    public void setRetryPaymentId(String retryPaymentId) {
        this.retryPaymentId = retryPaymentId;
    }

    public Document getReceiptImageDocument() {
        return receiptImageDocument;
    }

    public void setReceiptImageDocument(Document receiptImageDocument) {
        this.receiptImageDocument = receiptImageDocument;
    }

    public Document getReceiptPdfDocument() {
        return receiptPdfDocument;
    }

    public void setReceiptPdfDocument(Document receiptPdfDocument) {
        this.receiptPdfDocument = receiptPdfDocument;
    }

    public Boolean getReceiptNumberOldPattern() {
        return receiptNumberOldPattern;
    }

    public void setReceiptNumberOldPattern(Boolean receiptNumberOldPattern) {
        this.receiptNumberOldPattern = receiptNumberOldPattern;
    }

    public String getReceiptFullNumberBase36() {
        return receiptFullNumberBase36;
    }

    public void setReceiptFullNumberBase36(String receiptFullNumberBase36) {
        this.receiptFullNumberBase36 = receiptFullNumberBase36;
    }

    public Long getReceiptMainNumber() {
        return receiptMainNumber;
    }

    public void setReceiptMainNumber(Long receiptMainNumber) {
        this.receiptMainNumber = receiptMainNumber;
    }

    public String getReceiptMainNumberBase36() {
        return receiptMainNumberBase36;
    }

    public void setReceiptMainNumberBase36(String receiptMainNumberBase36) {
        this.receiptMainNumberBase36 = receiptMainNumberBase36;
    }

    public Boolean getRetried() {
        return retried;
    }

    public void setRetried(Boolean retried) {
        this.retried = retried;
    }
}
