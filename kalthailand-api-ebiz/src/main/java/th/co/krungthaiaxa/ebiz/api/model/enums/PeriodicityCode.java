package th.co.krungthaiaxa.ebiz.api.model.enums;

public enum PeriodicityCode {
    EVERY_MONTH, EVERY_QUARTER, EVERY_HALF_YEAR, EVERY_YEAR;

    public static PeriodicityCode getCode(String value) {
        for(PeriodicityCode periodicityCode : values()) {
            if (periodicityCode.toString().equalsIgnoreCase(value)) {
                return periodicityCode;
            }
        }
        return null;
    }
}
