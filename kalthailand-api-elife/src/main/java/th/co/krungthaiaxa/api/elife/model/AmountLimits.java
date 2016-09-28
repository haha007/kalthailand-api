package th.co.krungthaiaxa.api.elife.model;

import th.co.krungthaiaxa.api.elife.products.ProductAmounts;

/**
 * @author khoi.tran on 9/27/16.
 */
public class AmountLimits {
    private Amount minPremium;
    private Amount maxPremium;
    private Amount minSumInsured;
    private Amount maxSumInsured;

    public void copyToCommonData(CommonData destination) {
        destination.setMaxSumInsured(maxSumInsured);
        destination.setMinSumInsured(minSumInsured);
        destination.setMinPremium(minPremium);
        destination.setMaxPremium(maxPremium);
    }

    public void copyToProductAmounts(ProductAmounts destination) {
        destination.setMaxSumInsured(maxSumInsured);
        destination.setMinSumInsured(minSumInsured);
        destination.setMinPremium(minPremium);
        destination.setMaxPremium(maxPremium);
    }

    public Amount getMinPremium() {
        return minPremium;
    }

    public void setMinPremium(Amount minPremium) {
        this.minPremium = minPremium;
    }

    public Amount getMaxPremium() {
        return maxPremium;
    }

    public void setMaxPremium(Amount maxPremium) {
        this.maxPremium = maxPremium;
    }

    public Amount getMinSumInsured() {
        return minSumInsured;
    }

    public void setMinSumInsured(Amount minSumInsured) {
        this.minSumInsured = minSumInsured;
    }

    public Amount getMaxSumInsured() {
        return maxSumInsured;
    }

    public void setMaxSumInsured(Amount maxSumInsured) {
        this.maxSumInsured = maxSumInsured;
    }

}
