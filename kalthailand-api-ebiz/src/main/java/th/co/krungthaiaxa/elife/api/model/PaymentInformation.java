package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;

import java.time.LocalDate;

@ApiModel(description = "Data concerning the payment")
public class PaymentInformation {
    private LocalDate date;
    private Amount amount;
    private ChannelType channel;
    private String method;
    private SuccessErrorStatus status;
    private String creditCardName;
    private String rejectionErrorMessage;

    @ApiModelProperty(required = true, value = "Date of the payment")
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @ApiModelProperty(required = true, value = "Amount of the payment")
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    @ApiModelProperty(required = true, value = "Channel of the payment")
    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    @ApiModelProperty(required = true, value = "Method of the payment")
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @ApiModelProperty(required = true, value = "Status of the payment")
    public SuccessErrorStatus getStatus() {
        return status;
    }

    public void setStatus(SuccessErrorStatus status) {
        this.status = status;
    }

    @ApiModelProperty(required = true, value = "Credit card brand name used for the payment")
    public String getCreditCardName() {
        return creditCardName;
    }

    public void setCreditCardName(String creditCardName) {
        this.creditCardName = creditCardName;
    }

    @ApiModelProperty(required = true, value = "Rejection error message if the payment was rejected")
    public String getRejectionErrorMessage() {
        return rejectionErrorMessage;
    }

    public void setRejectionErrorMessage(String rejectionErrorMessage) {
        this.rejectionErrorMessage = rejectionErrorMessage;
    }
}
