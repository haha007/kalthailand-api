package th.co.krungthaiaxa.api.elife.products.iprotect;

import th.co.krungthaiaxa.api.elife.model.Amount;

import java.time.Instant;

/**
 * @author khoi.tran on 8/19/16.
 */
public class IProtectMomentCalculation {
    private Instant instant;
    /**
     * This is the total premiums which a customer has paid until this moment (instant).
     * The currency is same as the product currency.
     */
    private Amount totalPremiums;
    /**
     * When a customer want to cancel the policy at that point, they will receive the following money.
     * The currency is same as the product currency.
     */
    private Amount surrender;

    /**
     * If the insured is death at this point, the beneficiaries will receive the deathBenefit.
     * The currency is same as the product currency.
     */
    private Amount deathBenefit;

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Amount getTotalPremiums() {
        return totalPremiums;
    }

    public void setTotalPremiums(Amount totalPremiums) {
        this.totalPremiums = totalPremiums;
    }

    public Amount getSurrender() {
        return surrender;
    }

    public void setSurrender(Amount surrender) {
        this.surrender = surrender;
    }

    public Amount getDeathBenefit() {
        return deathBenefit;
    }

    public void setDeathBenefit(Amount deathBenefit) {
        this.deathBenefit = deathBenefit;
    }
}
