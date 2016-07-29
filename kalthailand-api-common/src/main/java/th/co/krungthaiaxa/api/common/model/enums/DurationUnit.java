package th.co.krungthaiaxa.api.common.model.enums;

import java.util.Calendar;

public enum DurationUnit {
    YEAR(0, "Year(s)", Calendar.YEAR),
    MONTH(1, "Month(s)", Calendar.MONTH),
    WEEK(2, "Week(s)", Calendar.WEEK_OF_YEAR),
    DAY(3, "Day(s)", Calendar.DAY_OF_YEAR),
    HOUR(4, "Hour(s)", Calendar.HOUR_OF_DAY),
    MINUTE(5, "Minute(s)", Calendar.MINUTE),
    SECOND(6, "Second(s)", Calendar.SECOND);

    private final int numValue;
    private final String label;
    private final int calendarField;

    DurationUnit(int numValue, String label, int calendarField) {
        this.numValue = numValue;
        this.label = label;
        this.calendarField = calendarField;
    }

    public String getLabel() {
        return label;
    }

    public int getNumValue() {
        return numValue;
    }

    public static DurationUnit valueOf(int numValue) {
        for (DurationUnit e : DurationUnit.values()) {
            if (e.numValue == numValue) {
                return e;
            }
        }

        return null;
    }

    public int getCalendarField() {
        return calendarField;
    }
}
