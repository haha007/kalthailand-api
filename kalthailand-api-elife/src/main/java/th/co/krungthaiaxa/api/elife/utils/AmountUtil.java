package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.api.common.exeption.AmountException;
import th.co.krungthaiaxa.api.common.utils.NumberUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;

/**
 * @author khoi.tran on 8/23/16.
 */
public class AmountUtil {
    public static boolean isBlank(Amount amount) {
        return (amount == null || amount.getValue() == null || StringUtils.isBlank(amount.getCurrencyCode()));
    }

    /**
     * Two amounts must have the same currency. Otherwise, throw Exception.
     *
     * @param amountA
     * @param amountB
     * @return a new instance of amount which has max value of amountA and B, and same currencyCode.
     */
    public static Amount max(Amount amountA, Amount amountB) {
        if (amountA.getCurrencyCode() != null) {
            if (!amountA.getCurrencyCode().equals(amountB.getCurrencyCode())) {
                throw new AmountException(String.format("Two amount cannot be compared when they have different currency: %s, %s", amountA, amountB));
            }
        }
        double max = Math.max(amountA.getValue(), amountB.getValue());
        return new Amount(max, amountA.getCurrencyCode());
    }

    public static String formatCurrency(Amount amount) {
        String result = NumberUtil.formatCurrencyValue(amount.getValue());
        if (StringUtils.isNotBlank(amount.getCurrencyCode())) {
            result += " " + amount.getCurrencyCode();
        }
        return result;
    }
}
