package th.co.krungthaiaxa.api.auth.test.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import th.co.krungthaiaxa.api.auth.KALApiAuth;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.data.UserList;
import th.co.krungthaiaxa.api.auth.model.UserDTO;
import th.co.krungthaiaxa.api.auth.repository.RoleRepository;
import th.co.krungthaiaxa.api.auth.repository.UserRepository;
import th.co.krungthaiaxa.api.auth.service.UserService;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tuong.le on 5/23/17.
 */

@SpringApplicationConfiguration(classes = KALApiAuth.class)
public class UserServiceTest extends BaseIntegrationResourceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserServiceTest.class);

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserList userList;

    @Inject
    private RoleRepository roleRepository;

    @Test
    public void can_create_new_user() {

        String[] roleString = {"API_ELIFE", "API_BLACKLIST", "UI_ELIFE_ADMIN"};

        UserDTO newUser = new UserDTO();
        newUser.setEmail("tuongle106@gmail.com");
        newUser.setUsername("elifeadminuser");
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
        final Optional<User> adminUser = userService.getOneActiveUserDetailByUsername("asdasasfasf");
        if (adminUser.isPresent()) {
            LOGGER.info(adminUser.get().getEmail());
            Assert.assertFalse(Objects.isNull(adminUser));
            return;
        }

        LOGGER.info("User 'elifeuser is not '");
    }

    @Test
    public void can_find_all_user_page() {
        final Pageable pageable = new PageRequest(0, 20);
        final Page<User> page = userService.getAllUser(pageable);
        Assert.assertTrue(page.getTotalPages() > 0);
    }

    @Test
    public void can_create_new_user_entity() {

        String[] roleString = {"API_ELIFE", "API_BLACKLIST", "API_SIGNING", "UI_USER"};

        User newUser = new User();
        newUser.setEmail("tuongle106@gmail.com");
        newUser.setUsername("elifeuser");
        newUser.setFirstName("Tuong");
        newUser.setLastName("Le");
        newUser.setActivated(Boolean.TRUE);
        newUser.setActivationKey(StringUtils.EMPTY);

        newUser.setPassword(passwordEncoder.encode("RswrV$@f^Gh*75"));

        Set<Role> roles = new HashSet<>();
        for (String s : roleString) {
            Role newRole = new Role();
            newRole.setId(s);
            newRole.setName(s);
            roles.add(newRole);
        }

        newUser.setRoles(roles);
        final User user = userRepository.save(newUser);

        Assert.assertTrue(!Objects.isNull(user));
    }

    @Test
    public void test_user_list() {
        Iterator<Role> iter = roleRepository.findAll().iterator();
        final List<Role> roles = new ArrayList<>();
        while (iter.hasNext())
            roles.add(iter.next());

        final List<User> userPersis = userList.getUsers().stream().map(user -> {
            final User newUser = new User();
            newUser.setEmail("tuongle106@gmail.com");
            newUser.setUsername(user.getUsername());
            newUser.setFirstName("Tuong");
            newUser.setLastName("Le");
            newUser.setActivated(Boolean.TRUE);
            newUser.setActivationKey(StringUtils.EMPTY);
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            Set<Role> roleSet = new HashSet<>();                                           
            user.getRoles().stream().forEach(roleFile -> {
                roleSet.add(roleRepository.findOne(roleFile.getId()));
            });
            newUser.setRoles(roleSet);
            return newUser;
        }).collect(Collectors.toList());
        
        userRepository.save(userPersis);
    }


}
