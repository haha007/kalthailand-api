package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PolicySetting;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.service.SettingService;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "Settings")
public class SettingResource {

    private final static Logger logger = LoggerFactory.getLogger(SettingResource.class);

    @Inject
    private SettingService settingService;

    @ApiOperation(value = "Get setting of policy", notes = "Get setting of policy.", response = Policy.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/setting/policy", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public PolicySetting getPoliciesQuota() {
        return settingService.loadPolicySetting();
    }
}
