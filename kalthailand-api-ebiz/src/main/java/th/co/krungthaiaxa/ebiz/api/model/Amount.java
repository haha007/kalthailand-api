package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "An amount with its currency")
public class Amount implements Serializable {
    private Double value;
    private String currencyCode;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return Objects.equals(value, amount.value) &&
                Objects.equals(currencyCode, amount.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currencyCode);
    }
}
