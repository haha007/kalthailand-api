package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.api.elife.model.Amount;

/**
 * @author khoi.tran on 8/23/16.
 */
public class AmountUtil {
    public static boolean isBlank(Amount amount) {
        return (amount == null || amount.getValue() == null || StringUtils.isBlank(amount.getCurrencyCode()));
    }
}
