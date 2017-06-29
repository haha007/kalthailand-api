package th.co.krungthaiaxa.api.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.jwt.JwtUserFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tuong.le on 5/23/17.
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Inject
    public UserDetailsServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    /**
     * The overried method is used for searching userDetail by username.
     *
     * @param username username
     * @return UserDetail
     * @throws UsernameNotFoundException default exception
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final Optional<User> userOptional = userService.getOneActiveUserDetailByUsername(username);
        final User user = userOptional.get();
        List<String> roles = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
        return JwtUserFactory.create(user.getUsername(), user.getPassword(), roles);
    }
}
