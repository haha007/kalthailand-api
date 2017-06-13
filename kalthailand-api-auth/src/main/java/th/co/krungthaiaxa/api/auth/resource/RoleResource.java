package th.co.krungthaiaxa.api.auth.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.auth.model.RoleDTO;
import th.co.krungthaiaxa.api.auth.service.RoleService;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author tuong.le on 5/31/17.
 */
@RestController
@Api(value = "Roles")
public class RoleResource {
    
    private final RoleService roleService;

    @Inject
    public RoleResource(RoleService roleService) {
        this.roleService = roleService;
    }

    @ApiOperation(value = "Get list of roles", notes = "Get list of role", response = RoleDTO.class)
    @RequestMapping(value = "/roles", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity getListOfUser() {
        final Pageable pageable = new PageRequest(0, 100);
        return ResponseEntity.ok(roleService.getAllRoles(pageable));
    }
}
