package th.co.krungthaiaxa.api.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author khoi.tran on 8/17/16.
 */
public class DateTimeUtil {

    public static ZoneId getThaiZoneId() {
        return ZoneId.of(ZoneId.SHORT_IDS.get("VST"));
    }

    public static LocalDate nowInThaiZoneId() {
        return LocalDate.now(getThaiZoneId());
    }
}
