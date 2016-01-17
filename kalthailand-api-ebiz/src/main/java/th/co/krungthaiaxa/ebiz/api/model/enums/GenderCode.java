package th.co.krungthaiaxa.ebiz.api.model.enums;

public enum GenderCode {
    MALE, FEMALE;

    public static GenderCode getCode(String value) {
        for(GenderCode genderCode : values()) {
            if (genderCode.toString().equalsIgnoreCase(value)) {
                return genderCode;
            }
        }
        return null;
    }

}
