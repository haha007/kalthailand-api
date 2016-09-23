package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

//import org.springframework.data.mongodb.core.mapping.Document;

/**
 * If a payment session is fail for any reason, the status will be {@link PaymentStatus#INCOMPLETE} or {@link PaymentStatus#OVERPAID}.
 * Then if payment is processed in the next time, it will create another payment object.
 */
@ApiModel(description = "The result of checking whether there's any newer completed payment or not.")
public class PaymentNewerCompletedResult {

    private Payment payment;
    @ApiModelProperty("")
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
