package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

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

    /**
     * @param delimiter
     * @param loopedString if loopedString is null, return empty string.
     * @param loopedTimes  the number of times which loopedString will be appended.
     * @return
     */
    public static String joinStrings(String delimiter, String loopedString, int loopedTimes) {
        StringBuilder result = new StringBuilder();
        if (loopedTimes > 0) {
            result.append(loopedString);
            for (int i = 1; i < loopedTimes; i++) {
                result.append(delimiter).append(loopedString);
            }
        }
        return result.toString();
    }

    public static String joinNotBlankStrings(String delimiter, String... strs) {
        if (strs == null) {
            return null;
        }
        if (strs.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (StringUtils.isNotBlank(str)) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(str);
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

    public static String toCamelCaseWithoutDelimiter(String string, Character delimiter) {
        if (StringUtils.isBlank(string)) {
            return string;
        }
        String result = WordUtils.capitalize(string.toLowerCase(), delimiter);
        return result.replace(delimiter.toString(), "");
    }

    public static String toCamelCaseWords(String string) {
        String separatedWordsByCamelCase = splitByCamelCase(string);
        String result = WordUtils.capitalize(separatedWordsByCamelCase);
        return result;
    }

    /**
     * @param string the string with camel case
     * @return this method will split the camel case string into separated words.
     * "lowercase",        // [lowercase]
     * "Class",            // [Class]
     * "MyClass",          // [My Class]
     * "HTML",             // [HTML]
     * "PDFLoader",        // [PDF Loader]
     * "AString",          // [A String]
     * "SimpleXMLParser",  // [Simple XML Parser]
     * "GL11Version",      // [GL 11 Version]
     * "99Bottles",        // [99 Bottles]
     * "May5",             // [May 5]
     * "BFG9000",          // [BFG 9000]
     */
    public static String splitByCamelCase(String string) {
        String result = string.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );//separated words
        return result.replaceAll("[ ]+", " ");
    }

    public static String formatNumberLength(long numberString, int length) {
        return StringUtils.leftPad(String.valueOf((int) numberString), length, '0');

    }

    public static String formatNumberLength(int numberString, int length) {
        return StringUtils.leftPad(String.valueOf(numberString), length, '0');
    }

    public static String formatNumberLength(String numberString, int length) {
        return StringUtils.leftPad(numberString, length, '0');
    }
}
