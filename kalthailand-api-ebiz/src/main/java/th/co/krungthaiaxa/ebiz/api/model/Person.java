package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;

import java.time.LocalDate;

@ApiModel(description = "Data concerning specific properties to a Person")
public class Person extends Party {
    private LocalDate birthDate;
    private GenderCode genderCode;

    @ApiModelProperty(value = "The person's birth date")
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @ApiModelProperty(value = "The person's gender")
    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
    }
}
