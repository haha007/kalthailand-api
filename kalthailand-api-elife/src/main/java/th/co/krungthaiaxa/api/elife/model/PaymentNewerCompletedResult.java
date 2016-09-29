package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

/**
 * If a payment session is fail for any reason, the status will be {@link PaymentStatus#INCOMPLETE} or {@link PaymentStatus#OVERPAID}.
 * Then if payment is processed in the next time, it will create another payment object.
 */
@ApiModel(description = "The result of checking whether there's any newer completed payment or not.")
public class PaymentNewerCompletedResult {
    @ApiModelProperty(required = true, notes = "This payment can be COMPLETED or not.")
    private Payment payment;
    @ApiModelProperty("This field store the newer payment which was COMPLETED. So it status is always COMPLETED and its dueDate is after the target payment's dueDate. Note: this payment can be the target payment.retryPaymentId or also can be paid after that. If this field is "
            + "null, it means that there's no newer payment which was COMPLETED after the target payment.")
    private Payment newerCompletedPayment;

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Payment getNewerCompletedPayment() {
        return newerCompletedPayment;
    }

    public void setNewerCompletedPayment(Payment newerCompletedPayment) {
        this.newerCompletedPayment = newerCompletedPayment;
    }
}
