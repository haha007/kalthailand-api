package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "iGen Life Insurance specific Premiums Data")
public class ProductIGenPremium implements Serializable {
    private Boolean sumInsuredOption;
    @Valid
    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by backend API if an amount is provided in financialScheduler.")
    @NotNull
    private Amount sumInsured;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total sum insured by the product before discount. This is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount sumInsuredBeforeDiscount = null;
    private List<DateTimeAmount> yearlyCashBacksForEndOfContract = new ArrayList<>();
    private List<DateTimeAmount> yearlyCashBacksForAnnual = new ArrayList<>();
    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive at the end of contract. This is the final value (after discount) and is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount endOfContractBenefit;

    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive when insured person is dead. This is the final value (after discount) and is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private List<DateTimeAmount> yearlyDeathBenefits = new ArrayList<>();
    @Valid
    @NotNull
    @ApiModelProperty(value = "Tax deduction per year.")
    private Amount yearlyTaxDeduction = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total tax deduction after all of premium years.")
    private Amount totalTaxDeduction = null;

    /**
     * View more in {@link ProductDividendOption}.
     */
    @ApiModelProperty(value = "Dividend option. Dividend is either given yearly in cash, or used for next premium or all of it at the end of the product duration.")
    private String dividendOptionId;

    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by backend API if an amount is provided in financialScheduler.")
    public Boolean getSumInsuredOption() {
        return sumInsuredOption;
    }

    public void setSumInsuredOption(Boolean sumInsuredOption) {
        this.sumInsuredOption = sumInsuredOption;
    }

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }

    @ApiModelProperty(value = "Yearly cash backs if cash option is chosen. This is calculated by backend API and cannot be set by client.")
    public List<DateTimeAmount> getYearlyCashBacksForEndOfContract() {
        return yearlyCashBacksForEndOfContract;
    }

    public void setYearlyCashBacksForEndOfContract(List<DateTimeAmount> yearlyCashBacksForEndOfContract) {
        this.yearlyCashBacksForEndOfContract = yearlyCashBacksForEndOfContract;
    }

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
        ProductIGenPremium that = (ProductIGenPremium) o;
        return Objects.equals(sumInsured, that.sumInsured) &&
                Objects.equals(sumInsuredBeforeDiscount, that.sumInsuredBeforeDiscount) &&
                Objects.equals(sumInsuredOption, that.sumInsuredOption) &&
                Objects.equals(endOfContractBenefit, that.endOfContractBenefit) &&
                Objects.equals(dividendOptionId, that.dividendOptionId) &&
                Objects.equals(totalTaxDeduction, that.totalTaxDeduction) &&
                Objects.equals(yearlyTaxDeduction, that.yearlyTaxDeduction) &&
                Objects.equals(yearlyCashBacksForEndOfContract, that.yearlyCashBacksForEndOfContract) &&
                Objects.equals(yearlyDeathBenefits, that.yearlyDeathBenefits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sumInsured, sumInsuredBeforeDiscount, sumInsuredOption,
                endOfContractBenefit,
                dividendOptionId,
                totalTaxDeduction,
                yearlyTaxDeduction,
                yearlyCashBacksForEndOfContract,
                yearlyDeathBenefits);
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

    public String getDividendOptionId() {
        return dividendOptionId;
    }

    public void setDividendOptionId(String dividendOptionId) {
        this.dividendOptionId = dividendOptionId;
    }

    public Amount getEndOfContractBenefit() {
        return endOfContractBenefit;
    }

    public void setEndOfContractBenefit(Amount endOfContractBenefit) {
        this.endOfContractBenefit = endOfContractBenefit;
    }

    public List<DateTimeAmount> getYearlyDeathBenefits() {
        return yearlyDeathBenefits;
    }

    public void setYearlyDeathBenefits(List<DateTimeAmount> yearlyDeathBenefits) {
        this.yearlyDeathBenefits = yearlyDeathBenefits;
    }

    public List<DateTimeAmount> getYearlyCashBacksForAnnual() {
        return yearlyCashBacksForAnnual;
    }

    public void setYearlyCashBacksForAnnual(List<DateTimeAmount> yearlyCashBacksForAnnual) {
        this.yearlyCashBacksForAnnual = yearlyCashBacksForAnnual;
    }
}
