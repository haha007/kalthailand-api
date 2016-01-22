package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Data concerning the payment")
public class Payment {
    private PaymentStatus status;
    private LocalDate dueDate;
    private LocalDate effectiveDate;
    private Amount amount;
    private List<PaymentInformation> paymentInformations;

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

    @ApiModelProperty(required = true, value = "The payment effective date. null if payment has not been done yet.")
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

    @ApiModelProperty(required = true, value = "List of payment status done for the specific payment. If not empty, " +
            "should contain 0 to N payments with status SUCCESS for whcih the sum of the amounts is equal to " +
            "expected amount. May contain 0 to N unsuccessful payments.")
    public List<PaymentInformation> getPaymentInformations() {
        return paymentInformations;
    }

    public void setPaymentInformations(List<PaymentInformation> paymentInformations) {
        this.paymentInformations = paymentInformations;
    }
}
