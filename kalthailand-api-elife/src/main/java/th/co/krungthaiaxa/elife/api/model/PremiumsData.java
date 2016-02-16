package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Data concerning premiums (price for the coverage and benefit agreed) to all commercial types",
        subTypes = {PremiumsDataLifeInsurance.class})
public class PremiumsData implements Serializable {
    private FinancialScheduler financialScheduler;
    private String productType;

    @ApiModelProperty(value = "The definition of how the premiums must be paid")
    public FinancialScheduler getFinancialScheduler() {
        return financialScheduler;
    }

    public void setFinancialScheduler(FinancialScheduler financialScheduler) {
        this.financialScheduler = financialScheduler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiumsData that = (PremiumsData) o;
        return Objects.equals(financialScheduler, that.financialScheduler) &&
                Objects.equals(productType, that.productType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(financialScheduler, productType);
    }
}
