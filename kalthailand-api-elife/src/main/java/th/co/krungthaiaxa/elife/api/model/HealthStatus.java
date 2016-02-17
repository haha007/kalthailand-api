package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

public class HealthStatus implements Serializable {
    private Integer heightInCm;
    private Integer weightInKg;
    private Boolean disableOrImmunoDeficient;
    private Boolean hospitalizedInLast6Months;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return Objects.equals(heightInCm, that.heightInCm) &&
                Objects.equals(weightInKg, that.weightInKg) &&
                Objects.equals(disableOrImmunoDeficient, that.disableOrImmunoDeficient) &&
                Objects.equals(hospitalizedInLast6Months, that.hospitalizedInLast6Months);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heightInCm, weightInKg, disableOrImmunoDeficient, hospitalizedInLast6Months);
    }
}
