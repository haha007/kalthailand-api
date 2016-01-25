package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@ApiModel(description = "An amount at a specific date with its currency")
public class DatedAmount implements Serializable {
    private Double value;
    private String currencyCode;
    private LocalDate date;

    @ApiModelProperty(required = true, value = "The amount")
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @ApiModelProperty(required = true, value = "Currency code ISO 4217")
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @ApiModelProperty(required = true, value = "Date of the amount")
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatedAmount that = (DatedAmount) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currencyCode, date);
    }
}
