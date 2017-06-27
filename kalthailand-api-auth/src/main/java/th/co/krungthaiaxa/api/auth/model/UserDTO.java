package th.co.krungthaiaxa.api.auth.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.common.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * @author tuong.le on 5/24/17.
 */
public class UserDTO {
    private String id;

    @NotNull
    @Pattern(regexp = Constants.USERNAME_REGEX, message = "incorrect format")
    @Size(min = 3, max = 100)
    private String username;

    @Email
    @NotNull
    @Size(max = 100)
    private String email;

    @NotNull
    @Size(max = 50)
    private String firstName;

    @NotNull
    @Size(max = 50)
    private String lastName;

    private boolean activated;

    @NotEmpty
    private Set<Role> roles;

    public UserDTO() {
        //Empty constructor
    }

    public UserDTO(final User user) {
        this.setUsername(user.getUsername());
        this.setId(user.getId());
        this.setEmail(user.getEmail());
        this.setActivated(user.isActivated());
        this.setRoles(user.getRoles());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(final boolean activated) {
        this.activated = activated;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
}
