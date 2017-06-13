package th.co.krungthaiaxa.api.auth.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.data.UserList;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @deprecated will be remove after testing backward compatible 
 */
@Deprecated
@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserList userList;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userList.getUsers().stream().filter(tmp -> tmp.getUsername().equals(username)).findFirst();
        if (user.isPresent()) {
            return JwtUserFactory.create(
                    user.get().getUsername(),
                    user.get().getPassword(),
                    user.get().getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        } else {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }
    }
}
