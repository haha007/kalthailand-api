package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author khoi.tran on 9/28/16.
 *         This class represent an amount of money is transfered (pay/withdraw...) in a specific date time. So that dateTime should be use UTC timezone and should run correctly when we have many servers run on many timezones.
 *         That's why this class use OffsetDateTime.
 */
@ApiModel(description = "An amount at a specific date with its currency")
public class DateTimeAmount implements Serializable {
    @NotNull
    private Instant dateTime;
    @Valid
    @NotNull
    private Amount amount;

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }
}
