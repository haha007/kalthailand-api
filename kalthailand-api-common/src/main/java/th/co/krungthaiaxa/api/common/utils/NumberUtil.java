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

    public static double consumed(Double total, Double remain) {
        if (total == null || total == 0 || remain == null) {
            return 0;
        }
        double consumed = total - remain;
        return consumed;
    }

    public static double consumedPercentage(Double total, Double remain) {
        double consumed = consumed(total, remain);
        return percentage(total, consumed);
    }

    public static double percentage(Double total, Double consumed) {
        if (total == null || total == 0 || consumed == null) {
            return 0;
        }
        return consumed / total * 100;
    }
}
