package th.co.krungthaiaxa.api.common.utils;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author khoi.tran on 9/19/16.
 */
public class StringUtilTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(StringUtilTest.class);

    @Test
    public void join_string_with_empty_value() {
        String[] strings = new String[] { "Aaa", " ", "Bbb", "", null, "Ccc" };
        String fullString = StringUtil.joinNotBlankStrings("1", strings);
        Assert.assertEquals("Aaa1Bbb1Ccc", fullString);
    }

    @Test
    public void join_string_with_empty_values() {
        String[] strings = new String[] { null, null };
        String fullString = StringUtil.joinNotBlankStrings("1", strings);
        Assert.assertEquals("", fullString);
    }

    @Test
    public void join_string_with_null() {
        String fullString = StringUtil.joinNotBlankStrings("1", null);
        Assert.assertNull(fullString);
    }

    @Test
    public void test_object_mapper() {
        List<Long> numbers = Arrays.asList(-1L, 0L, 10L);
        String string = ObjectMapperUtil.toSimpleStringMultiLineForEachElement(numbers);
        LOGGER.info(string);
        Assert.assertEquals("[\n"
                + "-1\n"
                + "0\n"
                + "10\n"
                + "]", string);
    }
}
