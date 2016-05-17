package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.DividendOption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "iGen Life Insurance specific Premiums Data")
public class ProductIGenPremium implements Serializable {
    private Boolean sumInsuredOption;
    private Amount sumInsured;
    private List<DatedAmount> yearlyCashBacks = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsMinimum = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsAverage = new ArrayList<>();
    private List<DatedAmount> endOfContractBenefitsMaximum = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksAverageDividende = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksMaximumDividende = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksAverageBenefit = new ArrayList<>();
    private List<DatedAmount> yearlyCashBacksMaximumBenefit = new ArrayList<>();
    private Amount yearlyTaxDeduction;
    private DividendOption dividendOption;

    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by back end API if an amount is provided in financialScheduler.")
    public Boolean getSumInsuredOption() {
        return sumInsuredOption;
    }

    public void setSumInsuredOption(Boolean sumInsuredOption) {
        this.sumInsuredOption = sumInsuredOption;
    }

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
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

    @ApiModelProperty(value = "Yearly average benefit. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacksAverageBenefit() {
        return yearlyCashBacksAverageBenefit;
    }

    public void setYearlyCashBacksAverageBenefit(List<DatedAmount> yearlyCashBacksAverageBenefit) {
        this.yearlyCashBacksAverageBenefit = yearlyCashBacksAverageBenefit;
    }

    @ApiModelProperty(value = "Yearly maximum benefit. This is calculated by back end API and cannot be set by client.")
    public List<DatedAmount> getYearlyCashBacksMaximumBenefit() {
        return yearlyCashBacksMaximumBenefit;
    }

    public void setYearlyCashBacksMaximumBenefit(List<DatedAmount> yearlyCashBacksMaximumBenefit) {
        this.yearlyCashBacksMaximumBenefit = yearlyCashBacksMaximumBenefit;
    }

    @ApiModelProperty(value = "Tax deduction per year.")
    public Amount getYearlyTaxDeduction() {
        return yearlyTaxDeduction;
    }

    public void setYearlyTaxDeduction(Amount yearlyTaxDeduction) {
        this.yearlyTaxDeduction = yearlyTaxDeduction;
    }

    @ApiModelProperty(value = "Dividend option. Dividend is either given yearly in cash, or used for next premium or all of it at the end of the product duration.")
    public DividendOption getDividendOption() {
        return dividendOption;
    }

    public void setDividendOption(DividendOption dividendOption) {
        this.dividendOption = dividendOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIGenPremium that = (ProductIGenPremium) o;
        return Objects.equals(sumInsured, that.sumInsured) &&
                Objects.equals(yearlyCashBacks, that.yearlyCashBacks) &&
                Objects.equals(endOfContractBenefitsMinimum, that.endOfContractBenefitsMinimum) &&
                Objects.equals(endOfContractBenefitsAverage, that.endOfContractBenefitsAverage) &&
                Objects.equals(endOfContractBenefitsMaximum, that.endOfContractBenefitsMaximum) &&
                Objects.equals(yearlyCashBacksAverageDividende, that.yearlyCashBacksAverageDividende) &&
                Objects.equals(yearlyCashBacksMaximumDividende, that.yearlyCashBacksMaximumDividende) &&
                Objects.equals(yearlyCashBacksAverageBenefit, that.yearlyCashBacksAverageBenefit) &&
                Objects.equals(yearlyCashBacksMaximumBenefit, that.yearlyCashBacksMaximumBenefit) &&
                Objects.equals(yearlyTaxDeduction, that.yearlyTaxDeduction) &&
                dividendOption == that.dividendOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sumInsured, yearlyCashBacks, endOfContractBenefitsMinimum, endOfContractBenefitsAverage, endOfContractBenefitsMaximum, yearlyCashBacksAverageDividende, yearlyCashBacksMaximumDividende, yearlyCashBacksAverageBenefit, yearlyCashBacksMaximumBenefit, yearlyTaxDeduction, dividendOption);
    }
}
