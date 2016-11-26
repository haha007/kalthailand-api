package th.co.krungthaiaxa.api.elife.system.health;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author khoi.tran on 11/26/16.
 */
@RestController
public class SystemHealthSettingResource {

    private final SystemHealthSettingService systemHealthSettingService;

    @Autowired
    public SystemHealthSettingResource(SystemHealthSettingService systemHealthSettingService) {this.systemHealthSettingService = systemHealthSettingService;}

    @ApiOperation(value = "Load setting of system health")
    @RequestMapping(value = "/system/health/setting", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public SystemHealthSetting loadSetting() {
        return systemHealthSettingService.loadSetting();
    }

    @ApiOperation(value = "Update setting of system health")
    @RequestMapping(value = "/system/health/setting", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public SystemHealthSetting updateSetting(@Valid @RequestBody SystemHealthSetting systemHealthSetting) {
        return systemHealthSettingService.saveSetting(systemHealthSetting);
    }
}
