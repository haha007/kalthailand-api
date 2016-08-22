package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "IProtect Life Insurance specific Premiums Data")
public class ProductIProtectPremium implements Serializable {
    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by back end API if an amount is provided in financialScheduler.")
    @NotNull
    private Boolean sumInsuredOption = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total sum insured by the product before discount. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount sumInsuredBeforeDiscount = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total sum insured by the product. This is the final value (after discount) and is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount sumInsured = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive when insured person is dead. This is the final value (after discount) and is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the "
            + "financialScheduler amount that is calculated")
    private Amount deathBenefit = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Tax deduction per year.")
    private Amount yearlyTaxDeduction = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total tax deduction after all of premium years.")
    private Amount totalTaxDeduction = null;

//    private List<IProtectMomentCalculation> yearlyCalculations = new ArrayList<>();

    //    private List<DatedAmount> yearlyCashBacks = new ArrayList<>();
//    private List<DatedAmount> endOfContractBenefitsMinimum = new ArrayList<>();
//    private List<DatedAmount> endOfContractBenefitsAverage = new ArrayList<>();
//    private List<DatedAmount> endOfContractBenefitsMaximum = new ArrayList<>();
//    private List<DatedAmount> yearlyCashBacksAverageDividende = new ArrayList<>();
//    private List<DatedAmount> yearlyCashBacksMaximumDividende = new ArrayList<>();
//    private List<DatedAmount> yearlyCashBacksAverageBenefit = new ArrayList<>();
//    private List<DatedAmount> yearlyCashBacksMaximumBenefit = new ArrayList<>();
//
//    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by back end API if an amount is provided in financialScheduler.")
    public Boolean getSumInsuredOption() {
        return sumInsuredOption;
    }

    public void setSumInsuredOption(Boolean sumInsuredOption) {
        this.sumInsuredOption = sumInsuredOption;
    }

    //    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }
//
//    @ApiModelProperty(value = "Yearly cash backs if cash option is chosen. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getYearlyCashBacks() {
//        return yearlyCashBacks;
//    }
//
//    public void setYearlyCashBacks(List<DatedAmount> yearlyCashBacks) {
//        this.yearlyCashBacks = yearlyCashBacks;
//    }
//
//    @ApiModelProperty(value = "Yearly minimum returns. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getEndOfContractBenefitsMinimum() {
//        return endOfContractBenefitsMinimum;
//    }
//
//    public void setEndOfContractBenefitsMinimum(List<DatedAmount> endOfContractBenefitsMinimum) {
//        this.endOfContractBenefitsMinimum = endOfContractBenefitsMinimum;
//    }
//
//    @ApiModelProperty(value = "Yearly average returns. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getEndOfContractBenefitsAverage() {
//        return endOfContractBenefitsAverage;
//    }
//
//    public void setEndOfContractBenefitsAverage(List<DatedAmount> endOfContractBenefitsAverage) {
//        this.endOfContractBenefitsAverage = endOfContractBenefitsAverage;
//    }
//
//    @ApiModelProperty(value = "Yearly maximum returns. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getEndOfContractBenefitsMaximum() {
//        return endOfContractBenefitsMaximum;
//    }
//
//    public void setEndOfContractBenefitsMaximum(List<DatedAmount> endOfContractBenefitsMaximum) {
//        this.endOfContractBenefitsMaximum = endOfContractBenefitsMaximum;
//    }
//
//    @ApiModelProperty(value = "Yearly average extra dividend. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getYearlyCashBacksAverageDividende() {
//        return yearlyCashBacksAverageDividende;
//    }
//
//    public void setYearlyCashBacksAverageDividende(List<DatedAmount> yearlyCashBacksAverageDividende) {
//        this.yearlyCashBacksAverageDividende = yearlyCashBacksAverageDividende;
//    }
//
//    @ApiModelProperty(value = "Yearly maximum extra dividend. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getYearlyCashBacksMaximumDividende() {
//        return yearlyCashBacksMaximumDividende;
//    }
//
//    public void setYearlyCashBacksMaximumDividende(List<DatedAmount> yearlyCashBacksMaximumDividende) {
//        this.yearlyCashBacksMaximumDividende = yearlyCashBacksMaximumDividende;
//    }
//
//    @ApiModelProperty(value = "Yearly average benefit. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getYearlyCashBacksAverageBenefit() {
//        return yearlyCashBacksAverageBenefit;
//    }
//
//    public void setYearlyCashBacksAverageBenefit(List<DatedAmount> yearlyCashBacksAverageBenefit) {
//        this.yearlyCashBacksAverageBenefit = yearlyCashBacksAverageBenefit;
//    }
//
//    @ApiModelProperty(value = "Yearly maximum benefit. This is calculated by back end API and cannot be set by client.")
//    public List<DatedAmount> getYearlyCashBacksMaximumBenefit() {
//        return yearlyCashBacksMaximumBenefit;
//    }
//
//    public void setYearlyCashBacksMaximumBenefit(List<DatedAmount> yearlyCashBacksMaximumBenefit) {
//        this.yearlyCashBacksMaximumBenefit = yearlyCashBacksMaximumBenefit;
//    }

    @ApiModelProperty(value = "Tax deduction per year.")
    public Amount getYearlyTaxDeduction() {
        return yearlyTaxDeduction;
    }

    public void setYearlyTaxDeduction(Amount yearlyTaxDeduction) {
        this.yearlyTaxDeduction = yearlyTaxDeduction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIProtectPremium that = (ProductIProtectPremium) o;
        return Objects.equals(sumInsuredBeforeDiscount, that.sumInsuredBeforeDiscount) &&
                Objects.equals(sumInsured, that.sumInsured) &&
                Objects.equals(deathBenefit, that.deathBenefit) &&
                Objects.equals(yearlyTaxDeduction, that.yearlyTaxDeduction) &&
                Objects.equals(totalTaxDeduction, that.totalTaxDeduction)
//                Objects.equals(yearlyCalculations, that.yearlyCalculations)
//
//                Objects.equals(yearlyCashBacks, that.yearlyCashBacks) &&
//                Objects.equals(endOfContractBenefitsMinimum, that.endOfContractBenefitsMinimum) &&
//                Objects.equals(endOfContractBenefitsAverage, that.endOfContractBenefitsAverage) &&
//                Objects.equals(endOfContractBenefitsMaximum, that.endOfContractBenefitsMaximum) &&
//                Objects.equals(yearlyCashBacksAverageDividende, that.yearlyCashBacksAverageDividende) &&
//                Objects.equals(yearlyCashBacksMaximumDividende, that.yearlyCashBacksMaximumDividende) &&
//                Objects.equals(yearlyCashBacksAverageBenefit, that.yearlyCashBacksAverageBenefit) &&
//                Objects.equals(yearlyCashBacksMaximumBenefit, that.yearlyCashBacksMaximumBenefit)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sumInsuredBeforeDiscount,
                sumInsured,
                yearlyTaxDeduction,
                totalTaxDeduction,
                deathBenefit
//                yearlyCalculations
//                yearlyCashBacks,
//                endOfContractBenefitsMinimum, endOfContractBenefitsAverage, endOfContractBenefitsMaximum,
//                yearlyCashBacksAverageDividende,
//                yearlyCashBacksMaximumDividende,
//                yearlyCashBacksAverageBenefit,
//                yearlyCashBacksMaximumBenefit
        );
    }

    /**
     * @return It's same as sumInsured.
     */
    public Amount getDeathBenefit() {
        return this.deathBenefit;
    }

    public void setDeathBenefit(Amount deathBenefit) {
        this.deathBenefit = deathBenefit;
    }

    public Amount getTotalTaxDeduction() {
        return totalTaxDeduction;
    }

    public void setTotalTaxDeduction(Amount totalTaxDeduction) {
        this.totalTaxDeduction = totalTaxDeduction;
    }

    public Amount getSumInsuredBeforeDiscount() {
        return sumInsuredBeforeDiscount;
    }

    public void setSumInsuredBeforeDiscount(Amount sumInsuredBeforeDiscount) {
        this.sumInsuredBeforeDiscount = sumInsuredBeforeDiscount;
    }

//    public List<IProtectMomentCalculation> getYearlyCalculations() {
//        return yearlyCalculations;
//    }
//
//    public void setYearlyCalculations(List<IProtectMomentCalculation> yearlyCalculations) {
//        this.yearlyCalculations = yearlyCalculations;
//    }
}
