package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.StringUtils;

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

    public static String maskEmail(String email) {
        return maskString(email, 3, 2);
    }
}
