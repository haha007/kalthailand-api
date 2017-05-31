package th.co.krungthaiaxa.api.auth.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import th.co.krungthaiaxa.api.auth.KALApiAuth;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.model.UserDTO;
import th.co.krungthaiaxa.api.auth.service.UserService;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author tuong.le on 5/23/17.
 */

@SpringApplicationConfiguration(classes = KALApiAuth.class)
public class UserServiceTest extends BaseIntegrationResourceTest {

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserService userService;

    @Test
    public void can_create_new_user() {

        String[] roleString = {"API_ELIFE", "API_BLACKLIST"};

        UserDTO newUser = new UserDTO();
        newUser.setEmail("tuongle106@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password"));
        newUser.setUsername("elifeuser123");
        newUser.setFirstName("Tuong");
        newUser.setLastName("Le");

        Set<Role> roles = new HashSet<>();
        for (String s : roleString) {
            Role newRole = new Role();
            newRole.setId(s);
            newRole.setName(s);
            roles.add(newRole);
        }

        newUser.setRoles(roles);
        final Optional<User> user = userService.createNewUser(newUser);

        Assert.assertTrue(user.isPresent());
    }

    @Test
    public void can_find_username() {
        final User adminUser = userService.getUserDetailByUsername("elifeadminuser");
        Assert.assertFalse(Objects.isNull(adminUser));
    }

    @Test
    public void can_find_all_user_page() {
        final Pageable pageable = new PageRequest(0, 20);
        final Page<User> page = userService.getAllUser(pageable);
        Assert.assertTrue(page.getTotalPages() > 0);
    }

}
