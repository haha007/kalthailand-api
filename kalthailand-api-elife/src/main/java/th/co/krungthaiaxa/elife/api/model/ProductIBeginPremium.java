package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "iBegin Life Insurance specific Premiums Data")
public class ProductIBeginPremium implements Serializable {
    private Amount sumInsured;

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIBeginPremium that = (ProductIBeginPremium) o;
        return Objects.equals(sumInsured, that.sumInsured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sumInsured);
    }
}
