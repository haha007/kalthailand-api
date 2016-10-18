package th.co.krungthaiaxa.api.elife.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.Periodicity;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author khoi.tran on 10/17/16.
 *         This class is only helpful for eLife only.
 */
@Component
public class ElifeEmailHelper {
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    private final static String DECIMAL_PATTERN = "#,##0.00";
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(DECIMAL_PATTERN);
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateTimeUtil.PATTERN_THAI_DATE);

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String toCurrencyValue(Double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public String toNormalDate(LocalDate localDate) {
        return DateTimeUtil.formatLocalDate(localDate, DateTimeUtil.PATTERN_THAI_DATE);
    }

    public String toThaiYear(Instant instant) {
        LocalDateTime localDateTime = DateTimeUtil.toThaiLocalDateTime(instant);
        return toThaiYear(localDateTime);
    }

    public String toThaiYear(LocalDateTime time) {
        return time.plusYears(543).format(DATE_TIME_FORMATTER);
    }

    public String toThaiPaymentMode(Periodicity due) {
        return messageSource.getMessage("payment.mode." + due.getCode().toString(), null, thLocale);
    }
}
