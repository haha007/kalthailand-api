package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author khoi.tran on 8/17/16.
 */
public class DateTimeUtil {

    public static ZoneId getThaiZoneId() {
        return ZoneId.of(ZoneId.SHORT_IDS.get("VST"));
    }

    public static LocalDate nowLocalDateInThaiZoneId() {
        return LocalDate.now(getThaiZoneId());
    }

    public static LocalDateTime nowLocalDateTimeInThaiZoneId() {
        return LocalDateTime.now(getThaiZoneId());
    }

    public static Instant toInstant(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static LocalDateTime toLocalDateTimePatternISO(String dateString) {
        LocalDateTime localDateTime;
        if (StringUtils.isNotBlank(dateString)) {
            localDateTime = LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateString));
        } else {
            localDateTime = null;
        }
        return localDateTime;
    }
}
