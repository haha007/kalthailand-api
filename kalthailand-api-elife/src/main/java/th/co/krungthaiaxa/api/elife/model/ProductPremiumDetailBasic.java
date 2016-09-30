package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 9/30/16.
 */
public class ProductPremiumDetailBasic {
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
    @Valid
    @NotNull
    @ApiModelProperty(value = "Tax deduction per year.")
    private Amount yearlyTaxDeduction = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "Total tax deduction after all of premium years.")
    private Amount totalTaxDeduction = null;
    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive at the end of contract. This is the final value (after discount) and is calculated by backend API if an amount is provided in financialScheduler."
            + " If dividendOption is END_OF_CONTRACT_PAY_BACK, then this value equals to the last element of yearlyCashBacksForEndOfContract."
            + " If dividendOption is ANNUAL_PAY_BACK_CASH or ANNUAL_PAY_BACK_NEXT_PREMIUM, then this value equals to the last element of yearlyCashBacksForAnnual."
    )
    private Amount endOfContractBenefit;
    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive when insured person is dead. "
            + "If death benefit is the same for every year, this field will have value. If death benefit is different for each year, those values will be set in yearlyDeathBenefits"
            + "This is the final value (after discount) and is calculated by back end API if an amount is provided in financialScheduler. If this value is provided, then it's the "
            + "financialScheduler amount that is calculated")
    private Amount deathBenefit = null;

    @Valid
    @NotNull
    @ApiModelProperty(value = "The money which beneficiaries will receive when insured person is dead. This is the final value (after discount) and is calculated by backend API if an amount is provided in financialScheduler. If this value is provided, then it's the financialScheduler amount that is calculated")
    private List<DateTimeAmount> yearlyDeathBenefits = new ArrayList<>();

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

    public Amount getSumInsuredBeforeDiscount() {
        return sumInsuredBeforeDiscount;
    }

    public void setSumInsuredBeforeDiscount(Amount sumInsuredBeforeDiscount) {
        this.sumInsuredBeforeDiscount = sumInsuredBeforeDiscount;
    }

    public Amount getYearlyTaxDeduction() {
        return yearlyTaxDeduction;
    }

    public void setYearlyTaxDeduction(Amount yearlyTaxDeduction) {
        this.yearlyTaxDeduction = yearlyTaxDeduction;
    }

    public Amount getTotalTaxDeduction() {
        return totalTaxDeduction;
    }

    public void setTotalTaxDeduction(Amount totalTaxDeduction) {
        this.totalTaxDeduction = totalTaxDeduction;
    }

    public Amount getEndOfContractBenefit() {
        return endOfContractBenefit;
    }

    public void setEndOfContractBenefit(Amount endOfContractBenefit) {
        this.endOfContractBenefit = endOfContractBenefit;
    }

    public Amount getDeathBenefit() {
        return deathBenefit;
    }

    public void setDeathBenefit(Amount deathBenefit) {
        this.deathBenefit = deathBenefit;
    }

    public List<DateTimeAmount> getYearlyDeathBenefits() {
        return yearlyDeathBenefits;
    }

    public void setYearlyDeathBenefits(List<DateTimeAmount> yearlyDeathBenefits) {
        this.yearlyDeathBenefits = yearlyDeathBenefits;
    }
}
