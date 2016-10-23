package th.co.krungthaiaxa.api.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author khoi.tran on 9/28/16.
 */
public class ListUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ListUtil.class);

    public static <T> T getLastItem(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T hasOneElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            if (list.size() > 1) {
                LOGGER.warn("The list should has only one element. Its elements: " + list.size());
            }
            return list.get(0);
        }
    }
}
