package th.co.krungthaiaxa.api.common.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;

import java.time.Instant;

/**
 * @author khoi.tran on 12/7/16.
 */
public class DateTimeUtilTest {
    @Test
    public void test_format_instant() {
        Instant instant = Instant.EPOCH;
        String formattedDate = DateTimeUtil.format(instant, "yyyy/MM>>dd hh~mm-ss SSS");
        Assert.assertEquals("1970/01>>01 08~00-00 000", formattedDate);
    }
}
