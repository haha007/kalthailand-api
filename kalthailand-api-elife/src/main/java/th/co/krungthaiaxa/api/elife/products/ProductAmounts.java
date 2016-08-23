package th.co.krungthaiaxa.api.elife.products;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;

/**
 * The mix/max values in ProductAmounts is used for limit the input values from client.
 * The mix/max values in CommonData is used for validate the calculated values.
 * They have different meaning.
 */
@ApiModel(description = "The mix/max values in ProductAmounts is used for limit the input values from client, while the mix/max values in CommonData is used for validate the calculated values. "
        + "Their values may be the same in most of cases, but their meaning are different.")
public class ProductAmounts {

    @Deprecated
    private Amount minPremium;
    @Deprecated
    private Amount maxPremium;
    @Deprecated
    private Amount minSumInsured;
    @Deprecated
    private Amount maxSumInsured;
    private CommonData commonData;

    @ApiModelProperty(required = true, value = "The minimum premium")
    public Amount getMinPremium() {
        return minPremium;
    }

    public void setMinPremium(Amount minPremium) {
        this.minPremium = minPremium;
    }

    @ApiModelProperty(required = true, value = "The maximum premium")
    public Amount getMaxPremium() {
        return maxPremium;
    }

    public void setMaxPremium(Amount maxPremium) {
        this.maxPremium = maxPremium;
    }

    @ApiModelProperty(required = true, value = "The minimum sum insured")
    public Amount getMinSumInsured() {
        return minSumInsured;
    }

    public void setMinSumInsured(Amount minSumInsured) {
        this.minSumInsured = minSumInsured;
    }

    @ApiModelProperty(required = true, value = "The maximum sum insured")
    public Amount getMaxSumInsured() {
        return maxSumInsured;
    }

    public void setMaxSumInsured(Amount maxSumInsured) {
        this.maxSumInsured = maxSumInsured;
    }

    @ApiModelProperty(required = true, value = "The static data of the product")
    public CommonData getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonData commonData) {
        this.commonData = commonData;
    }

}
