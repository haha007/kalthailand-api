package th.co.krungthaiaxa.ebiz.api.model;

import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;

import java.time.LocalDate;

public class Person extends Party {
    private LocalDate birthDate;
    private GenderCode genderCode;

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
    }
}
