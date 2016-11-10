package th.co.krungthaiaxa.api.common.model.authentication;

import java.io.Serializable;
import java.util.List;

/**
 * @author khoi.tran on 11/10/16.
 *         Need to implement {@link Serializable} because we need to save into session
 */
public class AuthenticatedUser implements Serializable {
    private String username;
    private String accessToken;
    private List<String> roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
