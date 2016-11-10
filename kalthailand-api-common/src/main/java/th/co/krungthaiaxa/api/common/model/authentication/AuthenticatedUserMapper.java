package th.co.krungthaiaxa.api.common.model.authentication;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 11/10/16.
 */
public class AuthenticatedUserMapper {
    public static AuthenticatedUser toAuthenticatedUser(UserDetails userDetails, String accessToken) {
        AuthenticatedUser result = new AuthenticatedUser();
        result.setUsername(userDetails.getUsername());
        if (userDetails.getAuthorities() != null) {
            List<String> roles = userDetails.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.toList());
            result.setRoles(roles);
        }

        result.setAccessToken(accessToken);
        return result;
    }
}
