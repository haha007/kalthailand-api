package th.co.krungthaiaxa.api.elife.system.health;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.elife.service.PolicyService;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author khoi.tran on 11/25/16.
 */
@RestController
public class SystemHealthResource {
    private final SystemHealthService healthCheckService;
    private final PolicyService policyService;

    @Autowired
    public SystemHealthResource(SystemHealthService healthCheckService, PolicyService policyService) {
        this.healthCheckService = healthCheckService;
        this.policyService = policyService;
    }

    @ApiOperation(value = "Load system's health status", notes = "Load system's health status: memory, space, CPU")
    @RequestMapping(value = "/system-health", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public SystemHealth getSystemHealth() {
        return healthCheckService.loadHealthStatus();
    }

    @ApiOperation(value = "Migrate MID to User Id that is used from Line V2",
            notes = "Collect all policies that don't have lineUserId, convert mid to userId, update database")
    @RequestMapping(value = "/migrate-mid-user-id", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity migrateMidToUserId() {
        Optional<Integer> resultOptional = policyService.updateLineUserIdOfPolicyId();
        if (resultOptional.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("modifiedCount", resultOptional.get()));
        }
        return ResponseEntity.badRequest().body(Collections.singletonMap("userMessage", "All policies had been updated"));
    }
}
