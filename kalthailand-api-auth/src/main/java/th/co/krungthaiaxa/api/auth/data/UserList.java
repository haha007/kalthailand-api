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

    public List<User> getUsers() {
        if (users != null) {
            return users;
        }

        users = new ArrayList<>();

        addUser(user1UserName, user1Password, user1Roles);
        addUser(user2UserName, user2Password, user2Roles);

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
