package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "iGen Life Insurance specific Premiums Data")
public class ProductIGenPremium implements Serializable {
    @ApiModelProperty(value = "True if the user has chosen to pay on a total Sum Insured base. False otherwise (premium choice). This is set by backend API if an amount is provided in financialScheduler.")
    private Boolean sumInsuredOption;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount sumInsured;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total sum insured by the product before discount. This is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private Amount sumInsuredBeforeDiscount = null;
    @ApiModelProperty(value = "Yearly cash backs if dividendOption is END_OF_CONTRACT_PAY_BACK. This is calculated by backend API and cannot be set by client.")
    private List<DateTimeAmount> yearlyCashBacksForEndOfContract = new ArrayList<>();
    @ApiModelProperty(value = "Yearly cash backs if dividendOption is ANNUAL_PAY_BACK_CASH or ANNUAL_PAY_BACK_NEXT_PREMIUM. This is calculated by backend API and cannot be set by client.")
    private List<DateTimeAmount> yearlyCashBacksForAnnual = new ArrayList<>();
    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive at the end of contract. This is the final value (after discount) and is calculated by backend API if an amount is provided in financialScheduler."
            + " If dividendOption is END_OF_CONTRACT_PAY_BACK, then this value equals to the last element of yearlyCashBacksForEndOfContract."
            + " If dividendOption is ANNUAL_PAY_BACK_CASH or ANNUAL_PAY_BACK_NEXT_PREMIUM, then this value equals to the last element of yearlyCashBacksForAnnual."
    )
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
    @ApiModelProperty(value = "END_OF_CONTRACT_PAY_BACK (1): dividend is given at the end of contract."
            + "ANNUAL_PAY_BACK_CASH(2): dividend is given yearly in cash."
            + "ANNUAL_PAY_BACK_NEXT_PREMIUM(3): dividend is given for next premium.")
    private String dividendOptionId;

    public Boolean getSumInsuredOption() {
        return sumInsuredOption;
    }

    public void setSumInsuredOption(Boolean sumInsuredOption) {
        this.sumInsuredOption = sumInsuredOption;
    }

    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }

    public List<DateTimeAmount> getYearlyCashBacksForEndOfContract() {
        return yearlyCashBacksForEndOfContract;
    }

    public void setYearlyCashBacksForEndOfContract(List<DateTimeAmount> yearlyCashBacksForEndOfContract) {
        this.yearlyCashBacksForEndOfContract = yearlyCashBacksForEndOfContract;
    }

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
                Objects.equals(yearlyCashBacksForAnnual, that.yearlyCashBacksForAnnual) &&
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
                yearlyCashBacksForAnnual,
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
