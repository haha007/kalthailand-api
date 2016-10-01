package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "iGen Life Insurance specific Premiums Data")
public class ProductIGenPremium extends PremiumDetail implements Serializable {

    @ApiModelProperty(value = "Yearly cash backs if dividendOption is END_OF_CONTRACT_PAY_BACK. This is calculated by backend API and cannot be set by client.")
    private List<DateTimeAmount> yearlyCashBacksForEndOfContract = new ArrayList<>();
    @ApiModelProperty(value = "Yearly cash backs if dividendOption is ANNUAL_PAY_BACK_CASH or ANNUAL_PAY_BACK_NEXT_PREMIUM. This is calculated by backend API and cannot be set by client.")
    private List<DateTimeAmount> yearlyCashBacksForAnnual = new ArrayList<>();

    /**
     * View more in {@link ProductDividendOption}.
     */
    @ApiModelProperty(value = "END_OF_CONTRACT_PAY_BACK (1): dividend is given at the end of contract."
            + "ANNUAL_PAY_BACK_CASH(2): dividend is given yearly in cash."
            + "ANNUAL_PAY_BACK_NEXT_PREMIUM(3): dividend is given for next premium.")
    private String dividendOptionId;

    public List<DateTimeAmount> getYearlyCashBacksForEndOfContract() {
        return yearlyCashBacksForEndOfContract;
    }

    public void setYearlyCashBacksForEndOfContract(List<DateTimeAmount> yearlyCashBacksForEndOfContract) {
        this.yearlyCashBacksForEndOfContract = yearlyCashBacksForEndOfContract;
    }

    public String getDividendOptionId() {
        return dividendOptionId;
    }

    public void setDividendOptionId(String dividendOptionId) {
        this.dividendOptionId = dividendOptionId;
    }

    public List<DateTimeAmount> getYearlyCashBacksForAnnual() {
        return yearlyCashBacksForAnnual;
    }

    public void setYearlyCashBacksForAnnual(List<DateTimeAmount> yearlyCashBacksForAnnual) {
        this.yearlyCashBacksForAnnual = yearlyCashBacksForAnnual;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
