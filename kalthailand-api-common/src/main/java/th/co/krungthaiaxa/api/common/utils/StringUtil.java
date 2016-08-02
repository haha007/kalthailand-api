package th.co.krungthaiaxa.api.common.utils;

/**
 * @author khoi.tran on 8/2/16.
 */
public class StringUtil {
    public static String newString(Object... strs) {
        StringBuilder sb = new StringBuilder();
        for (Object string : strs) {
            sb.append(string);
        }
        return sb.toString();
    }
}
