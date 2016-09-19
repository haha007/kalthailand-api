package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

    public static String joinNotBlankStrings(String delimiter, String... strs) {
        if (strs == null) {
            return null;
        }
        if (strs.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(strs[0]);
        for (int i = 1; i < strs.length; i++) {
            String str = strs[i];
            if (StringUtils.isNotBlank(str)) {
                sb.append(delimiter).append(str);
            }
        }
        return sb.toString();
    }

    /**
     * Applies the specified mask to the card number.
     *
     * @param string The string in plain format
     * @return The masked card number
     */
    public static String maskString(String string, int numShownPrefixChars, int numShownSuffixChars) {
        if (StringUtils.isBlank(string)) {
            return string;
        }
        StringBuilder maskedString = new StringBuilder();
        int length = string.length();
        int lastMaskedIndex = length - numShownSuffixChars;
        for (int i = 0; i < string.length(); i++) {
            if (i < numShownPrefixChars || i >= lastMaskedIndex) {
                maskedString.append(string.charAt(i));
            } else {
                maskedString.append("*");
            }
        }
        return maskedString.toString();
    }

    /**
     * This method is usually used in writing log.
     * We usually dont' want to write email information into logs files (because of security). So this method will help you to put some mask characters.
     *
     * @param email The original email, e.g. "myemail@axa.com"
     * @return masked email, e.g. "mye********om"
     */
    public static String maskEmail(String email) {
        return maskString(email, 3, 2);
    }

    /**
     * @param string the input string
     * @return split the input string by comma.
     * The result will never be null.
     * The result will never contain any null or empty element.
     */
    public static List<String> splitToNotNullStrings(String string) {
        List<String> result = split(string, ",");
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }

    public static List<String> split(String string, String separator) {
        List<String> result = null;
        if (string != null) {
            String[] array = StringUtils.splitByWholeSeparator(string, separator);
            result = new ArrayList<>(array.length);
            for (String s : array) {
                String item = s.trim();
                if (StringUtils.isNotBlank(item)) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}
