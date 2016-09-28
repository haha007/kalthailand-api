package th.co.krungthaiaxa.api.common.utils;

import java.util.List;

/**
 * @author khoi.tran on 9/28/16.
 */
public class ListUtil {
    public static <T> T getLastItem(List<T> list) {
        return list.get(list.size() - 1);
    }
}
