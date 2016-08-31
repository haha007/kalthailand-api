package th.co.krungthaiaxa.api.common.utils;

import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;

/**
 * @author khoi.tran on 8/31/16.
 */
public class EnumUtil {
    public static <T extends Enum<T>> T validateExistEnum(Class<T> enumClass, String enumName) {
        try {
            return Enum.valueOf(enumClass, enumName);
        } catch (IllegalArgumentException ex) {
            String msg = String.format("Not found enum instance. enumClass: %s, enumName: %s", enumClass, enumName);
            throw new BadArgumentException(msg, ex);
        }
    }
}
