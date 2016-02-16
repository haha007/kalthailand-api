package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Data concerning the payment")
@Document
public class Payment {
    @Id
    private String paymentId;
    private PaymentStatus status;
    private LocalDate dueDate;
    private LocalDate effectiveDate;
    private Amount amount;
    private String registrationKey;
    private List<PaymentInformation> paymentInformations = new ArrayList<>();

    // Used by Jackson
    public Payment() {
    }

    public Payment(Double value, String currencyCode, LocalDate dueDate) {
        this.status = PaymentStatus.FUTURE;
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

    @ApiModelProperty(value = "The payment's registration key in case of recurring payment. This is used by pay " +
            "gateway like LINE Pay.")
    public String getRegistrationKey() {
        return registrationKey;
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
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
}