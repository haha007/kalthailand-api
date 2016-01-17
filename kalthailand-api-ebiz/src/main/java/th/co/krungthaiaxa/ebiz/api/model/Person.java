package th.co.krungthaiaxa.ebiz.api.model;

import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;

public class Person {
    private GenderCode genderCode;

    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
    }
}
