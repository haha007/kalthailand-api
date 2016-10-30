package th.co.krungthaiaxa.api.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.utils.base36.Base36Number;
import th.co.krungthaiaxa.api.common.utils.base36.Base36Util;

/**
 * @author khoi.tran on 10/28/16.
 */
public class Base36NumberTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(Base36NumberTest.class);

    @Test
    public void test_convert_decimal_to_base36() {
        Assert.assertEquals(36, Base36Util.toDecimalLong("10"));
        Assert.assertEquals(35, Base36Util.toDecimalLong("z"));
        Assert.assertEquals(10, Base36Util.toDecimalLong("a"));
        Assert.assertEquals(9, Base36Util.toDecimalLong("9"));
        Assert.assertEquals(0, Base36Util.toDecimalLong("0"));
    }

    @Test
    public void test_convert_base36_to_decimal() {
        Assert.assertEquals("0", Base36Util.toBase36String(0));
        Assert.assertEquals("9", Base36Util.toBase36String(9));
        Assert.assertEquals("a", Base36Util.toBase36String(10));
        Assert.assertEquals("h", Base36Util.toBase36String(17));
        Assert.assertEquals("z", Base36Util.toBase36String(35));
        Assert.assertEquals("10", Base36Util.toBase36String(36));

        LOGGER.info("" + Base36Util.toBase36String(Long.MAX_VALUE));
        LOGGER.info("" + Base36Util.toBase36String(Long.MIN_VALUE));

    }

    @Test
    public void test_convert_max_value_of_base36l() {
        Assert.assertEquals(Long.MAX_VALUE, Base36Util.toDecimalLong(Base36Number.MAX_BASE36_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, Base36Number.MAX_VALUE.longValue());

        Assert.assertEquals(Long.MIN_VALUE, Base36Util.toDecimalLong(Base36Number.MIN_BASE36_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, Base36Number.MIN_VALUE.longValue());

    }

    @Test
    public void test_convert_overflow_number() {
        Assert.assertEquals(Long.MAX_VALUE, Base36Number.MAX_VALUE.longValue());
        Assert.assertEquals(-1, Base36Number.MAX_VALUE.intValue());//because of overflow number
        Assert.assertEquals(9.0f, new Base36Number("9").floatValue(), 0.0001);
        Assert.assertEquals(35.0, new Base36Number("00z").doubleValue(), 0.0001);
        Assert.assertEquals(1295.0, new Base36Number("0zz").doubleValue(), 0.0001);

    }

    @Test
    public void test_hashcode() {
        Assert.assertEquals(new Base36Number("9zab").hashCode(), new Base36Number("9zab").hashCode());
        Assert.assertEquals(new Base36Number("00z").hashCode(), new Base36Number("z").hashCode());
        Assert.assertEquals(new Base36Number("0z").hashCode(), new Base36Number(35).hashCode());
        Assert.assertNotEquals(new Base36Number("a").hashCode(), new Base36Number("10").hashCode());
    }

    @Test
    public void test_compare_and_equals() {
        Assert.assertTrue(new Base36Number("a").compareTo(new Base36Number("10")) < 0);
        Assert.assertTrue(new Base36Number("a").compareTo(new Base36Number(10)) == 0);
        Assert.assertTrue(new Base36Number("a").compareTo(new Base36Number("9")) > 0);

        Assert.assertTrue(new Base36Number("a").equals(new Base36Number(10)));
        Assert.assertTrue(!new Base36Number("a").equals(new Base36Number("10")));
    }

    @Test
    public void test_clone() {
        Base36Number base36Number = new Base36Number(145);
        Base36Number cloned = base36Number.clone();
        Assert.assertEquals(base36Number, cloned);
        Assert.assertEquals(base36Number.getBase36Value(), cloned.getBase36Value());
        Assert.assertEquals(base36Number.longValue(), cloned.longValue());
        Assert.assertNotSame(base36Number, cloned);
    }
}
