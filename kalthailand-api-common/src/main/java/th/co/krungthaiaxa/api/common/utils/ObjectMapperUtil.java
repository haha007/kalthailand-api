package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Provides functionality for reading and writing object to Json, String.
 * The converting to json will be helpful when working with messaging (e.g. SQS, SNS).
 */
public final class ObjectMapperUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperUtil.class);

    private ObjectMapperUtil() {
    }

    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        return ReflectionToStringBuilder.toString(object, ToStringStyle.DEFAULT_STYLE);
    }

    /**
     * This method is usually for logging only.
     *
     * @param object
     * @return
     */
    public static String toStringMultiLine(Object object) {
        if (object == null) {
            return null;
        }
        return ReflectionToStringBuilder.toString(object, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * This method is usually for logging only.
     *
     * @param list
     * @return
     */
    public static String toStringMultiLineForEachElement(List<?> list) {
        if (list == null) {
            return null;
        }
        StringBuilder result = new StringBuilder("[\n");
        for (Object element : list) {
            result.append(toStringMultiLine(element));
            result.append(", ");
        }
        result.append("\n]");
        return result.toString();
    }
}
