package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;

public class ExceptionUtils {
    public static void notNull(Object object, ElifeException elifeException) throws ElifeException {
        if (object == null) {
            throw elifeException;
        }
    }

    public static void notNull(Object object, BaseException elifeException) {
        if (object == null) {
            throw elifeException;
        }
    }

    public static void isEqual(Object object1, Object object2, ElifeException elifeException) throws ElifeException {
        if (!object1.equals(object2)) {
            throw elifeException;
        }
    }

    public static void isEqual(Object object1, Object object2, BaseException elifeException) {
        if (!object1.equals(object2)) {
            throw elifeException;
        }
    }

    public static void isNotEqual(Object object1, Object object2, ElifeException elifeException) throws ElifeException {
        if (object1.equals(object2)) {
            throw elifeException;
        }
    }

    public static void isNotEqual(Object object1, Object object2, BaseException elifeException) {
        if (object1.equals(object2)) {
            throw elifeException;
        }
    }

    public static void isTrue(Boolean aBoolean, ElifeException elifeException) throws ElifeException {
        if (!aBoolean) {
            throw elifeException;
        }
    }

    public static void isTrue(Boolean aBoolean, BaseException elifeException) {
        if (!aBoolean) {
            throw elifeException;
        }
    }

    public static void isFalse(Boolean aBoolean, ElifeException elifeException) throws ElifeException {
        if (aBoolean) {
            throw elifeException;
        }
    }

    public static void isFalse(Boolean aBoolean, BaseException elifeException) {
        if (aBoolean) {
            throw elifeException;
        }
    }

}
