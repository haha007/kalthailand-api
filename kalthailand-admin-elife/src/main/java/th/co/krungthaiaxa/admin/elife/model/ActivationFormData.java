package th.co.krungthaiaxa.admin.elife.model;

import org.hibernate.validator.constraints.NotEmpty;
import th.co.krungthaiaxa.api.common.utils.Constants;

import javax.validation.constraints.Pattern;

/**
 * @author tuong.le on 6/2/17.
 */
public class ActivationFormData {

    @NotEmpty
    private String activationKey;

    @NotEmpty
    @Pattern(regexp = Constants.PASSWORD_REGEX,
            message = "At least 8 characters long, " +
                    "Contain at least 1 uppercase, " +
                    "Contain at least 1 alphanumeric, " +
                    "Contain at least 1 special character, " +
                    "Cannot be sequential number or alphabet")
    private String password;

    @NotEmpty
    private String confirmPassword;

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
