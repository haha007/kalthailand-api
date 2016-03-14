package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;

import java.time.LocalDate;
import java.util.Objects;

@ApiModel(description = "Data concerning the payment")
public class PaymentInformation {
    private LocalDate date;
    private Amount amount;
    private ChannelType channel;
    private String method;
    private SuccessErrorStatus status;
    private String creditCardName;
    private String rejectionErrorCode;
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

    @ApiModelProperty(required = true, value = "Rejection error code if the payment was rejected")
    public String getRejectionErrorCode() {
        return rejectionErrorCode;
    }

    public void setRejectionErrorCode(String rejectionErrorCode) {
        this.rejectionErrorCode = rejectionErrorCode;
    }

    @ApiModelProperty(required = true, value = "Rejection error message if the payment was rejected")
    public String getRejectionErrorMessage() {
        return rejectionErrorMessage;
    }

    public void setRejectionErrorMessage(String rejectionErrorMessage) {
        this.rejectionErrorMessage = rejectionErrorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentInformation that = (PaymentInformation) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(amount, that.amount) &&
                channel == that.channel &&
                Objects.equals(method, that.method) &&
                status == that.status &&
                Objects.equals(creditCardName, that.creditCardName) &&
                Objects.equals(rejectionErrorCode, that.rejectionErrorCode) &&
                Objects.equals(rejectionErrorMessage, that.rejectionErrorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount, channel, method, status, creditCardName, rejectionErrorCode, rejectionErrorMessage);
    }
}
