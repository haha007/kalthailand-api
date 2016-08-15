package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.jsoup.helper.StringUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.utils.RsaUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning the payment")
@Document(collection = "payment")
public class Payment {
    @Id
    private String paymentId;
    private String policyId;
    private String orderId;
    private String transactionId;
    private String registrationKey;
    private PaymentStatus status;
    private LocalDate dueDate;
    private LocalDate effectiveDate;
    private Amount amount;
    private List<PaymentInformation> paymentInformations = new ArrayList<>();

    // Used by Jackson
    public Payment() {
    }

    public Payment(String policyId, Double value, String currencyCode, LocalDate dueDate) {
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
        return (StringUtil.isBlank(registrationKey) ? registrationKey : RsaUtil.decrypt(registrationKey));
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = (StringUtil.isBlank(registrationKey) ? registrationKey : RsaUtil.encrypt(registrationKey));
    }

    @ApiModelProperty(required = true, value = "Status of the payment")
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @ApiModelProperty(required = true, value = "The payment due date. Can be in future.")
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @ApiModelProperty(value = "The payment effective date. null if payment has not been done yet.")
    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @ApiModelProperty(required = true, value = "The payment's amount.")
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    @ApiModelProperty(value = "List of payment status done for the specific payment. If not empty, " +
            "should contain 0 to N payments with status SUCCESS for whcih the sum of the amounts is equal to " +
            "expected amount. May contain 0 to N unsuccessful payments.")
    public List<PaymentInformation> getPaymentInformations() {
        return paymentInformations;
    }

    public void addPaymentInformation(PaymentInformation paymentInformation) {
        paymentInformations.add(paymentInformation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId) &&
                status == payment.status &&
                Objects.equals(dueDate, payment.dueDate) &&
                Objects.equals(effectiveDate, payment.effectiveDate) &&
                Objects.equals(amount, payment.amount) &&
                Objects.equals(registrationKey, payment.registrationKey) &&
                Objects.equals(paymentInformations, payment.paymentInformations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, status, dueDate, effectiveDate, amount, registrationKey, paymentInformations);
    }
}
