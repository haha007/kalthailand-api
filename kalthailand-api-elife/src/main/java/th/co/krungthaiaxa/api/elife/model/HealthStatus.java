package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

public class HealthStatus implements Serializable {
    private Integer heightInCm;
    private Integer weightInKg;
    private Boolean disableOrImmunoDeficient;
    private Boolean hospitalizedInLast6Months;
    private Boolean deniedOrCounterOffer;
    private Boolean weightChangeInLast6Months;
    private String weightChangeInLast6MonthsReason;

    @ApiModelProperty(value = "The person's height in centimeters")
    public Integer getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(Integer heightInCm) {
        this.heightInCm = heightInCm;
    }

    @ApiModelProperty(value = "The person's height in kilograms")
    public Integer getWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(Integer weightInKg) {
        this.weightInKg = weightInKg;
    }

    @ApiModelProperty(value = "Flag for insured disability and HIV status")
    public Boolean getDisableOrImmunoDeficient() {
        return disableOrImmunoDeficient;
    }

    public void setDisableOrImmunoDeficient(Boolean disableOrImmunoDeficient) {
        this.disableOrImmunoDeficient = disableOrImmunoDeficient;
    }

    @ApiModelProperty(value = "Flag for insured being hospitalized in the last 6 months")
    public Boolean getHospitalizedInLast6Months() {
        return hospitalizedInLast6Months;
    }

    public void setHospitalizedInLast6Months(Boolean hospitalizedInLast6Months) {
        this.hospitalizedInLast6Months = hospitalizedInLast6Months;
    }

    @ApiModelProperty(value = "Has been denied a policy before or had counter offer with higher premium")
    public Boolean getDeniedOrCounterOffer() {
        return deniedOrCounterOffer;
    }

    public void setDeniedOrCounterOffer(Boolean deniedOrCounterOffer) {
        this.deniedOrCounterOffer = deniedOrCounterOffer;
    }

    @ApiModelProperty(value = "Weight has changed in the last 6 months")
    public Boolean getWeightChangeInLast6Months() {
        return weightChangeInLast6Months;
    }

    public void setWeightChangeInLast6Months(Boolean weightChangeInLast6Months) {
        this.weightChangeInLast6Months = weightChangeInLast6Months;
    }

    @ApiModelProperty(value = "Reason for weight change in the last 6 months")
    public String getWeightChangeInLast6MonthsReason() {
        return weightChangeInLast6MonthsReason;
    }

    public void setWeightChangeInLast6MonthsReason(String weightChangeInLast6MonthsReason) {
        this.weightChangeInLast6MonthsReason = weightChangeInLast6MonthsReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return Objects.equals(heightInCm, that.heightInCm) &&
                Objects.equals(weightInKg, that.weightInKg) &&
                Objects.equals(disableOrImmunoDeficient, that.disableOrImmunoDeficient) &&
                Objects.equals(hospitalizedInLast6Months, that.hospitalizedInLast6Months) &&
                Objects.equals(deniedOrCounterOffer, that.deniedOrCounterOffer) &&
                Objects.equals(weightChangeInLast6Months, that.weightChangeInLast6Months) &&
                Objects.equals(weightChangeInLast6MonthsReason, that.weightChangeInLast6MonthsReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heightInCm, weightInKg, disableOrImmunoDeficient, hospitalizedInLast6Months, deniedOrCounterOffer, weightChangeInLast6Months, weightChangeInLast6MonthsReason);
    }
}
