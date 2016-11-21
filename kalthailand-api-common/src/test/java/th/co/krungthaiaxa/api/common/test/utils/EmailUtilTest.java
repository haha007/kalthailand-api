package th.co.krungthaiaxa.api.common.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.EmailUtil;

/**
 * @author khoi.tran on 11/18/16.
 */
public class EmailUtilTest {
    @Test
    public void testEmail() {
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@ca"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@c.c"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@c.com"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc+1.0@c.com"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc.dad-abc_dax+1.0@cac-xyz_ax0+1.com"));

        Assert.assertFalse(EmailUtil.isValidEmailAddress("abcc.c"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("@a"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abcc.c@"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abcc.c@&1"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abc&a@c.com"));
    }
}
