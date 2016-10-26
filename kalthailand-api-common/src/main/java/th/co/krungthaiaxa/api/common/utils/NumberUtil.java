package th.co.krungthaiaxa.api.common.utils;

import java.text.DecimalFormat;

/**
 * @author khoi.tran on 10/26/16.
 */
public class NumberUtil {
    public static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,##0.00");

    public static String formatCurrencyValue(double value) {
        return CURRENCY_FORMATTER.format(value);
    }
}
