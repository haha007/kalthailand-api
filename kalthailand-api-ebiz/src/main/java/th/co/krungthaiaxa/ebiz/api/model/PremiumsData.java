package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Data concerning premiums (price for the coverage and benefit agreed) to all commercial types",
        subTypes = {PremiumsDataLifeInsurance.class})
public class PremiumsData {
    private FinancialScheduler financialScheduler;
    private String productType;

    @ApiModelProperty(value = "The definition of how the premiums must be paid")
    public FinancialScheduler getFinancialScheduler() {
        return financialScheduler;
    }

    public void setFinancialScheduler(FinancialScheduler financialScheduler) {
        this.financialScheduler = financialScheduler;
    }
}
