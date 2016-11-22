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
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@c.co"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@c.com"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc@ca.co"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc+1.0@c.com"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc+50.dad01-abc_dax@cac-xyzax01.com"));
        Assert.assertTrue(EmailUtil.isValidEmailAddress("abc.dad01-abc_dax+01.1@cac-xyzax01.com"));

        Assert.assertFalse("Email doesn't have '.' after '@': '@x.xx'", EmailUtil.isValidEmailAddress("abc@ca"));
        Assert.assertFalse("Email must have at least 2 characters after '.': '@x.xx'", EmailUtil.isValidEmailAddress("abc@c.c"));
        Assert.assertFalse("Email must have '@'", EmailUtil.isValidEmailAddress("abcc.c"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("@a"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abcc.c@"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abcc.c@&1"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abc&a@c.com"));
        Assert.assertFalse(EmailUtil.isValidEmailAddress("abca@axa@axa.com"));

    }
}
