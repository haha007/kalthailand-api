package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

@ApiModel(description = "An amount at a specific date with its currency")
public class DatedAmount {
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
}
