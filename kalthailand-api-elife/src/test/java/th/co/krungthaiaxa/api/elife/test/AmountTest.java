package th.co.krungthaiaxa.api.elife.test;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.utils.ThaiBahtUtil;

/**
 * @author khoi.tran on 10/30/16.
 */
public class AmountTest {
    @Test
    public void test_clone() {
        double originalValue = 10.5;
        String originalCurrency = ThaiBahtUtil.CURRENCY_THB;
        Amount amount = new Amount(originalValue, originalCurrency);
        Amount cloned = amount.clone();
        Assert.assertNotSame(amount, cloned);
        Assert.assertEquals(amount.getValue(), cloned.getValue());
        Assert.assertEquals(amount.getCurrencyCode(), cloned.getCurrencyCode());

        amount.setValue(11.5);
        amount.setCurrencyCode("USD");

        Assert.assertEquals(originalValue, cloned.getValue(), 0.0001);
        Assert.assertEquals(originalCurrency, cloned.getCurrencyCode());
    }
}
