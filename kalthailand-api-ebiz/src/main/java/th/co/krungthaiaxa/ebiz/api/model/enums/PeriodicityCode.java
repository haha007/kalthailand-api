package th.co.krungthaiaxa.ebiz.api.model.enums;

public enum PeriodicityCode {
    EVERY_DAY, EVERY_WEEK, EVERY_TWO_WEEKS, EVERY_MONTH, EVERY_QUARTER, EVERY_FOUR_MONTHS, EVERY_HALF_YEAR, EVERY_YEAR, UNIQUE;

    public static PeriodicityCode getCode(String value) {
        for(PeriodicityCode periodicityCode : values()) {
            if (periodicityCode.toString().equalsIgnoreCase(value)) {
                return periodicityCode;
            }
        }
        return null;
    }
}
