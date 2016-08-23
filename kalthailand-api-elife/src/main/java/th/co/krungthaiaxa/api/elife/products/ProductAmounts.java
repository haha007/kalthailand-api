package th.co.krungthaiaxa.api.elife.products;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;

//TODO I don't understand what's this object for? min/max premium and min/max sumInsured already defined in commonData. Why don't just use commonData?
@ApiModel(description = "Data common to all quotes commercial types")
public class ProductAmounts {
    /**
     * @deprecated please use {@link CommonData#getMinPremium()}.
     */
    @Deprecated
    private Amount minPremium;
    /**
     * @deprecated please use {@link CommonData#getMaxPremium()}.
     */
    @Deprecated
    private Amount maxPremium;
    /**
     * @deprecated please use {@link CommonData#getMinSumInsured()}.
     */
    @Deprecated
    private Amount minSumInsured;
    /**
     * @deprecated please use {@link CommonData#getMaxSumInsured()}.
     */
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
