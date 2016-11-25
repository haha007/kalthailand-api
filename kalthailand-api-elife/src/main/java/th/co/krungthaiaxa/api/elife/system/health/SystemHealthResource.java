package th.co.krungthaiaxa.api.elife.system.health;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author khoi.tran on 11/25/16.
 */
@RestController
public class SystemHealthResource {
    private final SystemHealthService healthCheckService;

    @Autowired
    public SystemHealthResource(SystemHealthService healthCheckService) {this.healthCheckService = healthCheckService;}

    @ApiOperation(value = "Load system's health status", notes = "Load system's health status: memory, space, CPU")
    @RequestMapping(value = "/system-health", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public SystemHealth getSystemHealth() {
        return healthCheckService.loadHealthStatus();
    }
}
