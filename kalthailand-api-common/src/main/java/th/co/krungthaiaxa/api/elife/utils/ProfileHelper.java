package th.co.krungthaiaxa.api.elife.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * This is the helper, not utils.
 * A utils contains only static methods, while a helper can inject other beans.
 */
@Component
public class ProfileHelper {
    public static final String NO_PROFILE = "none";
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfileHelper.class);
    private final Environment env;
    private String[] usingProfiles = null;

    @Inject
    public ProfileHelper(Environment env) {this.env = env;}

    public String[] getUsingProfiles() {
        if (usingProfiles == null) {
            usingProfiles = env.getActiveProfiles();
            if (usingProfiles.length == 0) {
                usingProfiles = env.getDefaultProfiles();
                if (usingProfiles.length == 0) {
                    LOGGER.warn("There's something really wrong with your Spring Profile configuration. There's no information in both activeProfiles and defaultProfiles.");
                    usingProfiles = new String[] { NO_PROFILE };
                }
            }
        }
        return usingProfiles;
    }

    public String getFirstUsingProfile() {
        String[] profiles = getUsingProfiles();
        return profiles[0];
    }
}
