package th.co.krungthaiaxa.api.auth.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import th.co.krungthaiaxa.api.auth.KALApiAuth;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.service.UserService;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Objects;
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
        
        String[] roleString = {"UI_ELIFE_ADMIN","API_ELIFE","API_BLACKLIST","API_SIGNING","UI_AUTOPAY","UI_VALIDATION","UI_SLC","UI_CAMPAIGN"};
        
        User newUser = new User();
        newUser.setEmail("tuongle106@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password"));
        newUser.setUsername("elifeadminuser");
        
        Set<Role> roles = new HashSet<>();
        for(String s : roleString){
            Role newRole = new Role();
            newRole.setName(s);
            roles.add(newRole);
        }
        
        newUser.setRoles(roles);
        final User user = userService.createNewUser(newUser);
        
        Assert.assertTrue(user.equals(newUser));
    }

    @Test
    public void can_find_username() {
        final User adminUser = userService.getUserDetailByUsername("elifeadminuser");
        Assert.assertFalse(Objects.isNull(adminUser));
    }
}
