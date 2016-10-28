package th.co.krungthaiaxa.api.common.utils.base36;

/**
 * @author khoi.tran on 10/28/16.
 */
public class Base36Util {
    public static String toBase36String(long decimalNumber) {
        return Long.toString(decimalNumber, Base36Number.RADIX);
    }

    public static long toDecimalLong(String base36String) {
        return Long.valueOf(base36String, Base36Number.RADIX);
    }
}
