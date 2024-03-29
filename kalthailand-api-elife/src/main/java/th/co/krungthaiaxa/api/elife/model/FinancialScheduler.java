package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@ApiModel(description = "The definition of how the premiums must be paid")
public class FinancialScheduler implements Serializable {
    @ApiModelProperty(value = "The periodicity of the scheduled payment")
    private Periodicity periodicity;
    /**
     * This field has value only if a customer input sumInsured (sumInsured before discount won't have value).
     * If the customer input premium, then modelAmount will be equal to premium, and sumInsured won't have value.
     */
    @ApiModelProperty(value = "The premiums value (not discount yet) which customer must pay in a periodicity (month, quarter, year...). This is calculated by back end API if a sum insured amount is provided in premiumsDataLifeInsurance. If this value is provided, then it's the premiumsDataLifeInsurance sum insured that is calculated")
    private Amount modalAmountBeforeDiscount;
    @ApiModelProperty(value = "The premiums value (after discount) which customer must pay in a periodicity (month, quarter, year...).  This is calculated by back end API if a sum insured amount is provided in premiumsDataLifeInsurance. If this value is provided, then it's the premiumsDataLifeInsurance sum insured that is calculated")
    private Amount modalAmount;
    @ApiModelProperty(value = "End date of the financial scheduler, e.g. date of end premium payment. This is calculated by back end API and cannot be set by client.")
    private LocalDate endDate;
    //TODO migrate: all monthly payment must has atpMode is checked (AUTOPAY)
    private Integer atpMode;

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    public Amount getModalAmount() {
        return modalAmount;
    }

    public void setModalAmount(Amount modalAmount) {
        this.modalAmount = modalAmount;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialScheduler that = (FinancialScheduler) o;
        return Objects.equals(modalAmountBeforeDiscount, that.modalAmountBeforeDiscount) &&
                Objects.equals(modalAmount, that.modalAmount) &&
                Objects.equals(periodicity, that.periodicity) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                modalAmountBeforeDiscount,
                modalAmount,
                periodicity,
                endDate);
    }

    public Amount getModalAmountBeforeDiscount() {
        return modalAmountBeforeDiscount;
    }

    public void setModalAmountBeforeDiscount(Amount modalAmountBeforeDiscount) {
        this.modalAmountBeforeDiscount = modalAmountBeforeDiscount;
    }

    public Integer getAtpMode() {
        return atpMode;
    }

    public void setAtpMode(Integer atpMode) {
        this.atpMode = atpMode;
    }
}
