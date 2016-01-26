package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@ApiModel(description = "The definition of how the premiums must be paid")
public class FinancialScheduler implements Serializable {
    private Periodicity periodicity;
    private Amount modalAmount;
    private LocalDate endDate = LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST")));

    @ApiModelProperty(value = "The periodicity of the scheduled payment")
    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    @ApiModelProperty(value = "A regularly scheduled payment amount contractually obligated by the policy")
    public Amount getModalAmount() {
        return modalAmount;
    }

    public void setModalAmount(Amount modalAmount) {
        this.modalAmount = modalAmount;
    }

    @ApiModelProperty(value = "End date of the financial scheduler, e.g. date of end premium payment")
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
        return Objects.equals(periodicity, that.periodicity) &&
                Objects.equals(modalAmount, that.modalAmount) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodicity, modalAmount, endDate);
    }
}