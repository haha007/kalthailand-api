package th.co.krungthaiaxa.api.elife.test.products;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

/**
 * @author khoi.tran on 11/18/16.
 */
public class ProductUtilTest {
    @Test
    public void testEmail() {
        Assert.assertTrue(ProductUtils.isValidEmailAddress("abc@ca"));
        Assert.assertTrue(ProductUtils.isValidEmailAddress("abc@c.c"));
        Assert.assertTrue(ProductUtils.isValidEmailAddress("abc@c.com"));
        Assert.assertTrue(ProductUtils.isValidEmailAddress("abc+1.0@c.com"));
        Assert.assertTrue(ProductUtils.isValidEmailAddress("abc.dad-abc_dax+1.0@cac-xyz_ax0+1.com"));

        Assert.assertFalse(ProductUtils.isValidEmailAddress("abcc.c"));
        Assert.assertFalse(ProductUtils.isValidEmailAddress("@a"));
        Assert.assertFalse(ProductUtils.isValidEmailAddress("abcc.c@"));
        Assert.assertFalse(ProductUtils.isValidEmailAddress("abcc.c@&1"));
        Assert.assertFalse(ProductUtils.isValidEmailAddress("abc&a@c.com"));
    }
}
