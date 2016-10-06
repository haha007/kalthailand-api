package th.co.krungthaiaxa.api.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author khoi.tran on 10/6/16.
 */
public class ProjectProfileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectProfileUtil.class);

    public static String getDefaultProfile() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile != null) {
            LOGGER.info("Running with Spring profile(s) : {}", profile);
            return profile;
        }

        LOGGER.warn("No Spring profile configured, running with default configuration");
        return "dev";
    }
}
