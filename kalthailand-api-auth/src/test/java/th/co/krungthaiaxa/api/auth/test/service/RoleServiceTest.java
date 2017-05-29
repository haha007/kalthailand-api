package th.co.krungthaiaxa.api.auth.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import th.co.krungthaiaxa.api.auth.KALApiAuth;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.service.RoleService;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @author tuong.le on 5/23/17.
 */

@SpringApplicationConfiguration(classes = KALApiAuth.class)
public class RoleServiceTest extends BaseIntegrationResourceTest {

    @Inject
    private RoleService roleService;

    @Test
    public void can_create_new_authority() {
        String[] roleString = {"UI_ELIFE_ADMIN","API_ELIFE","API_BLACKLIST","API_SIGNING","UI_AUTOPAY","UI_VALIDATION","UI_SLC","UI_CAMPAIGN"};
        
        for(String r : roleString){
            Role newRole = new Role();
            newRole.setId(r);
            newRole.setName(r);
            roleService.createAuthority(newRole);
        }
    }

    @Test
    public void can_find_authority() {
        final Role role = roleService.getAuthorityById("API_ELIFE");
        Assert.assertFalse(Objects.isNull(role));
    }
}
