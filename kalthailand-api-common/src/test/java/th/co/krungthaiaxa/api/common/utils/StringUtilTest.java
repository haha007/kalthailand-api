package th.co.krungthaiaxa.api.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author khoi.tran on 9/19/16.
 */
public class StringUtilTest {
    @Test
    public void join_string_with_empty_value() {
        String[] strings = new String[] { "Aaa", " ", "Bbb", "", null, "Ccc" };
        String fullString = StringUtil.joinNotBlankStrings("1", strings);
        Assert.assertEquals("Aaa1Bbb1Ccc", fullString);
    }
}
