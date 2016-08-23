package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "An amount with its currency")
public class Amount implements Serializable {
    @NotNull
    private Double value;
    @NotBlank
    private String currencyCode;

    /**
     * NEVER remove this. It's required for Json constructor & DB persistence.
     */
    public Amount() {
    }

    public Amount(Double value, String currencyCode) {
        this.value = value;
        this.currencyCode = currencyCode;
    }

    public Amount multiply(double factor) {
        return new Amount(this.getValue() * factor, this.getCurrencyCode());
    }

    public static Amount amount(double value, String currency) {
        Amount result = new Amount();
        result.setCurrencyCode(currency);
        result.setValue(value);
        return result;
    }

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

    @Override
    public String toString() {
        return value + " " + currencyCode;
    }
}
