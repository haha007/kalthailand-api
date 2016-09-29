package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ThaiBuddhistDate;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * @author khoi.tran on 8/17/16.
 */
public class DateTimeUtil {
    public static final String PATTERN_FILEPATH = "yyyyMMdd";
    public static final String PATTERN_THAI_DATE = "dd/MM/yyyy";
    public static final String PATTERN_THAI_DATE_TIME = "dd/MM/yyyy hh:mm:ss";

    public static String formatBuddhistThaiDate(LocalDate localDate) {
        ThaiBuddhistDate tdate = ThaiBuddhistDate.from(localDate);
        return tdate.format(ofPattern(PATTERN_THAI_DATE));
    }

    public static String formatThaiDate(LocalDate localDate) {
        return localDate.format(ofPattern(PATTERN_THAI_DATE));
    }

    public static String formatThaiDate(LocalDateTime localDate) {
        return localDate.format(ofPattern(PATTERN_THAI_DATE));
    }

    public static String formatThaiDateTime(LocalDateTime localDate) {
        return localDate.format(ofPattern(PATTERN_THAI_DATE_TIME));
    }

    public static String formatBuddhistThaiDateTime(LocalDateTime localDateTime) {
        ThaiBuddhistDate tdate = ThaiBuddhistDate.from(localDateTime);
        return tdate.format(ofPattern(PATTERN_THAI_DATE_TIME));
    }

    public static String formatBuddhistThaiDate(LocalDateTime localDateTime) {
        ThaiBuddhistDate tdate = ThaiBuddhistDate.from(localDateTime);
        return tdate.format(ofPattern(PATTERN_THAI_DATE));
    }

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

    public static String formatLocalDate(LocalDate localDate, String pattern) {
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatNowForFilePath() {
        return formatLocalDate(LocalDate.now(), PATTERN_FILEPATH);
    }

    public static Instant plusYears(Instant instant, int year) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        localDateTime = localDateTime.plusYears(year);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
