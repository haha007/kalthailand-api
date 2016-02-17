package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning premiums (price for the coverage and benefit agreed) specific to " +
        "Life Insurance", parent = PremiumsData.class)
public class PremiumsDataLifeInsurance extends PremiumsData implements Serializable {
    private Amount lifeInsuranceSumInsured;
    private List<DatedAmount> yearlyCashBacks = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsMinimum = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsAverage = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsMaximum = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksMinimumDividende = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksAverageDividende = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksMaximumDividende = new ArrayList<>();

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getLifeInsuranceSumInsured() {
        return lifeInsuranceSumInsured;
    }

    public void setLifeInsuranceSumInsured(Amount lifeInsuranceSumInsured) {
        this.lifeInsuranceSumInsured = lifeInsuranceSumInsured;
    }

    @ApiModelProperty(value = "Yearly cash backs if cash option is chosen. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacks() {
        return yearlyCashBacks;
    }

    public void setYearlyCashBacks(List<DatedAmount> yearlyCashBacks) {
        this.yearlyCashBacks = yearlyCashBacks;
    }

    @ApiModelProperty(value = "Yearly minimum returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getEndOfContractBenefitsMinimum() {
        return endOfContractBenefitsMinimum;
    }

    public void setEndOfContractBenefitsMinimum(List<DatedAmount> endOfContractBenefitsMinimum) {
        this.endOfContractBenefitsMinimum = endOfContractBenefitsMinimum;
    }

    @ApiModelProperty(value = "Yearly average returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getEndOfContractBenefitsAverage() {
        return endOfContractBenefitsAverage;
    }

    public void setEndOfContractBenefitsAverage(List<DatedAmount> endOfContractBenefitsAverage) {
        this.endOfContractBenefitsAverage = endOfContractBenefitsAverage;
    }

    @ApiModelProperty(value = "Yearly maximum returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getEndOfContractBenefitsMaximum() {
        return endOfContractBenefitsMaximum;
    }

    public void setEndOfContractBenefitsMaximum(List<DatedAmount> endOfContractBenefitsMaximum) {
        this.endOfContractBenefitsMaximum = endOfContractBenefitsMaximum;
    }

    @ApiModelProperty(value = "Yearly minimum extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacksMinimumDividende() {
        return yearlyCashBacksMinimumDividende;
    }

    public void setYearlyCashBacksMinimumDividende(List<DatedAmount> yearlyCashBacksMinimumDividende) {
        this.yearlyCashBacksMinimumDividende = yearlyCashBacksMinimumDividende;
    }

    @ApiModelProperty(value = "Yearly average extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacksAverageDividende() {
        return yearlyCashBacksAverageDividende;
    }

    public void setYearlyCashBacksAverageDividende(List<DatedAmount> yearlyCashBacksAverageDividende) {
        this.yearlyCashBacksAverageDividende = yearlyCashBacksAverageDividende;
    }

    @ApiModelProperty(value = "Yearly maximum extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacksMaximumDividende() {
        return yearlyCashBacksMaximumDividende;
    }

    public void setYearlyCashBacksMaximumDividende(List<DatedAmount> yearlyCashBacksMaximumDividende) {
        this.yearlyCashBacksMaximumDividende = yearlyCashBacksMaximumDividende;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiumsDataLifeInsurance that = (PremiumsDataLifeInsurance) o;
        return Objects.equals(lifeInsuranceSumInsured, that.lifeInsuranceSumInsured) &&
                Objects.equals(yearlyCashBacks, that.yearlyCashBacks) &&
                Objects.equals(endOfContractBenefitsMinimum, that.endOfContractBenefitsMinimum) &&
                Objects.equals(endOfContractBenefitsAverage, that.endOfContractBenefitsAverage) &&
                Objects.equals(endOfContractBenefitsMaximum, that.endOfContractBenefitsMaximum) &&
                Objects.equals(yearlyCashBacksMinimumDividende, that.yearlyCashBacksMinimumDividende) &&
                Objects.equals(yearlyCashBacksAverageDividende, that.yearlyCashBacksAverageDividende) &&
                Objects.equals(yearlyCashBacksMaximumDividende, that.yearlyCashBacksMaximumDividende);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lifeInsuranceSumInsured, yearlyCashBacks, endOfContractBenefitsMinimum, endOfContractBenefitsAverage, endOfContractBenefitsMaximum, yearlyCashBacksMinimumDividende, yearlyCashBacksAverageDividende, yearlyCashBacksMaximumDividende);
    }
}
