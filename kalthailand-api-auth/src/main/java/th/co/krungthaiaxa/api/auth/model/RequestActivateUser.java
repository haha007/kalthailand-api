package th.co.krungthaiaxa.api.auth.model;

import th.co.krungthaiaxa.api.common.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author tuong.le on 6/6/17.
 */
public class RequestActivateUser {
    @NotNull
    private String activationKey;

    @NotNull
    @Pattern(regexp = Constants.PASSWORD_REGEX, 
            message = "At least 8 characters long, " +
                    "Contain at least 1 uppercase, " +
                    "Contain alphanumeric, " +
                    "Contain 1 special character, " +
                    "Cannot be sequential number or alphabet")
    private String password;

    @NotNull
    private String confirmPassword;

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
