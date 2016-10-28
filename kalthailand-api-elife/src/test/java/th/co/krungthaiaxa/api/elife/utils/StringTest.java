package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

/**
 * @author khoi.tran on 8/4/16.
 */
public class StringTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(StringTest.class);

    @Test
    public void test_mask_email_never_throw_exception() {
        String result = StringUtil.maskEmail(null);
        Assert.assertNull(result);
        result = StringUtil.maskEmail("");
        Assert.assertTrue(StringUtils.isBlank(result));
        StringUtil.maskEmail(" ");
        Assert.assertTrue(StringUtils.isBlank(result));

        result = StringUtil.maskEmail("abcdef.xyzt@12345.com");
        Assert.assertEquals("abc****************om", result);

        result = StringUtil.maskEmail("abc@ab");
        Assert.assertEquals("abc*ab", result);

        result = StringUtil.maskEmail("abc@ab ");
        Assert.assertEquals("abc**b ", result);

        result = StringUtil.maskEmail("abc@b");
        Assert.assertEquals("abc@b", result);

        result = StringUtil.maskEmail("a@b");
        Assert.assertEquals("a@b", result);
    }
}
