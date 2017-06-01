package th.co.krungthaiaxa.api.auth.utils;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author tuong.le on 5/24/17.
 */
public class RandomUtil {
    private static final int DEF_COUNT = 20;

    public static String generateResetKey() {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
    }

    public static String generateActivationKey() {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
    }
}
