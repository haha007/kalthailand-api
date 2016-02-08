package th.co.krungthaiaxa.ebiz.api.model;

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
    private List<DatedAmount> lifeInsuranceYearlyCashBacks = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceMinimumYearlyReturns = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceAverageYearlyReturns = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceMaximumYearlyReturns = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceMinimumExtraDividende = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceAverageExtraDividende = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceMaximumExtraDividende = new ArrayList<>();

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getLifeInsuranceSumInsured() {
        return lifeInsuranceSumInsured;
    }

    public void setLifeInsuranceSumInsured(Amount lifeInsuranceSumInsured) {
        this.lifeInsuranceSumInsured = lifeInsuranceSumInsured;
    }

    @ApiModelProperty(value = "Yearly cash backs if cash option is chosen. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceYearlyCashBacks() {
        return lifeInsuranceYearlyCashBacks;
    }

    public void setLifeInsuranceYearlyCashBacks(List<DatedAmount> lifeInsuranceYearlyCashBacks) {
        this.lifeInsuranceYearlyCashBacks = lifeInsuranceYearlyCashBacks;
    }

    @ApiModelProperty(value = "Yearly minimum returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceMinimumYearlyReturns() {
        return lifeInsuranceMinimumYearlyReturns;
    }

    public void setLifeInsuranceMinimumYearlyReturns(List<DatedAmount> lifeInsuranceMinimumYearlyReturns) {
        this.lifeInsuranceMinimumYearlyReturns = lifeInsuranceMinimumYearlyReturns;
    }

    @ApiModelProperty(value = "Yearly average returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceAverageYearlyReturns() {
        return lifeInsuranceAverageYearlyReturns;
    }

    public void setLifeInsuranceAverageYearlyReturns(List<DatedAmount> lifeInsuranceAverageYearlyReturns) {
        this.lifeInsuranceAverageYearlyReturns = lifeInsuranceAverageYearlyReturns;
    }

    @ApiModelProperty(value = "Yearly maximum returns. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceMaximumYearlyReturns() {
        return lifeInsuranceMaximumYearlyReturns;
    }

    public void setLifeInsuranceMaximumYearlyReturns(List<DatedAmount> lifeInsuranceMaximumYearlyReturns) {
        this.lifeInsuranceMaximumYearlyReturns = lifeInsuranceMaximumYearlyReturns;
    }

    @ApiModelProperty(value = "Yearly minimum extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceMinimumExtraDividende() {
        return lifeInsuranceMinimumExtraDividende;
    }

    public void setLifeInsuranceMinimumExtraDividende(List<DatedAmount> lifeInsuranceMinimumExtraDividende) {
        this.lifeInsuranceMinimumExtraDividende = lifeInsuranceMinimumExtraDividende;
    }

    @ApiModelProperty(value = "Yearly average extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceAverageExtraDividende() {
        return lifeInsuranceAverageExtraDividende;
    }

    public void setLifeInsuranceAverageExtraDividende(List<DatedAmount> lifeInsuranceAverageExtraDividende) {
        this.lifeInsuranceAverageExtraDividende = lifeInsuranceAverageExtraDividende;
    }

    @ApiModelProperty(value = "Yearly maximum extra dividend. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getLifeInsuranceMaximumExtraDividende() {
        return lifeInsuranceMaximumExtraDividende;
    }

    public void setLifeInsuranceMaximumExtraDividende(List<DatedAmount> lifeInsuranceMaximumExtraDividende) {
        this.lifeInsuranceMaximumExtraDividende = lifeInsuranceMaximumExtraDividende;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiumsDataLifeInsurance that = (PremiumsDataLifeInsurance) o;
        return Objects.equals(lifeInsuranceSumInsured, that.lifeInsuranceSumInsured) &&
                Objects.equals(lifeInsuranceYearlyCashBacks, that.lifeInsuranceYearlyCashBacks) &&
                Objects.equals(lifeInsuranceMinimumYearlyReturns, that.lifeInsuranceMinimumYearlyReturns) &&
                Objects.equals(lifeInsuranceAverageYearlyReturns, that.lifeInsuranceAverageYearlyReturns) &&
                Objects.equals(lifeInsuranceMaximumYearlyReturns, that.lifeInsuranceMaximumYearlyReturns) &&
                Objects.equals(lifeInsuranceMinimumExtraDividende, that.lifeInsuranceMinimumExtraDividende) &&
                Objects.equals(lifeInsuranceAverageExtraDividende, that.lifeInsuranceAverageExtraDividende) &&
                Objects.equals(lifeInsuranceMaximumExtraDividende, that.lifeInsuranceMaximumExtraDividende);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lifeInsuranceSumInsured, lifeInsuranceYearlyCashBacks, lifeInsuranceMinimumYearlyReturns, lifeInsuranceAverageYearlyReturns, lifeInsuranceMaximumYearlyReturns, lifeInsuranceMinimumExtraDividende, lifeInsuranceAverageExtraDividende, lifeInsuranceMaximumExtraDividende);
    }
}
