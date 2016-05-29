package th.co.krungthaiaxa.api.auth.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserList {
    private List<User> users;
    @Value("${kal.api.user.1.name}")
    private String user1UserName;
    @Value("${kal.api.user.1.password}")
    private String user1Password;
    @Value("${kal.api.user.1.role}")
    private String user1Roles;
    @Value("${kal.api.user.2.name}")
    private String user2UserName;
    @Value("${kal.api.user.2.password}")
    private String user2Password;
    @Value("${kal.api.user.2.role}")
    private String user2Roles;
    @Value("${kal.api.user.3.name}")
    private String user3UserName;
    @Value("${kal.api.user.3.password}")
    private String user3Password;
    @Value("${kal.api.user.3.role}")
    private String user3Roles;
    @Value("${kal.api.user.4.name}")
    private String user4UserName;
    @Value("${kal.api.user.4.password}")
    private String user4Password;
    @Value("${kal.api.user.4.role}")
    private String user4Roles;
    @Value("${kal.api.user.5.name}")
    private String user5UserName;
    @Value("${kal.api.user.5.password}")
    private String user5Password;
    @Value("${kal.api.user.5.role}")
    private String user5Roles;

    public List<User> getUsers() {
        if (users != null) {
            return users;
        }

        users = new ArrayList<>();

        addUser(user1UserName, user1Password, user1Roles);
        addUser(user2UserName, user2Password, user2Roles);
        addUser(user3UserName, user3Password, user3Roles);
        addUser(user4UserName, user4Password, user4Roles);
        addUser(user5UserName, user5Password, user5Roles);

        return users;
    }

    private void addUser(String userName, String password, String roles) {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        getRoles(roles).stream().forEach(user::addRole);
        users.add(user);
    }

    private List<String> getRoles(String roles) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(roles)) {
            return result;
        }
        Collections.addAll(result, roles.split(","));
        return result;
    }
}
