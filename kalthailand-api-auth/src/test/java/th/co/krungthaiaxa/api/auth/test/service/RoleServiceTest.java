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
        Role newRole = new Role();
        newRole.setName("API_SIGNING");                  
        final Role createdRole = roleService.createAuthority(newRole);
        Assert.assertTrue(createdRole.equals(newRole));
    }

    @Test
    public void can_find_authority() {
        final Role role = roleService.getAuthorityByName("API_SIGNING");
        Assert.assertFalse(Objects.isNull(role));
    }
}
