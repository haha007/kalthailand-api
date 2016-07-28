package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.service.PolicyNumberSettingService;

import javax.inject.Inject;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "PolicyNumberSetting")
public class PolicyNumberSettingResource {

    private final static Logger logger = LoggerFactory.getLogger(PolicyNumberSettingResource.class);

    @Inject
    private PolicyNumberSettingService policyNumberSettingService;

    @ApiOperation(value = "Get setting of policy number", notes = "Get setting of policy number.", response = PolicyNumberSetting.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policy-numbers/setting", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public PolicyNumberSetting getPolicyNumberSetting() {
        return policyNumberSettingService.loadSetting();
    }

    //TODO should be removed
    @ApiOperation(value = "Get setting of policy number", notes = "Get setting of policy number.", response = PolicyNumberSetting.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policy-numbers/setting/{id}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public PolicyNumberSetting getPolicyNumberSetting(@PathVariable String id) {
        return policyNumberSettingService.loadSetting();
    }

    @ApiOperation(value = "Update setting of policy number", notes = "Update setting of policy number.", response = PolicyNumberSetting.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policy-numbers/setting", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public void updatePolicyNumberSetting(@Valid @RequestBody PolicyNumberSetting policyNumberSetting) {
        policyNumberSettingService.updateSetting(policyNumberSetting);
    }

    //TODO should be removed
    @ApiOperation(value = "Update setting of policy number", notes = "Update setting of policy number.", response = PolicyNumberSetting.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policy-numbers/setting/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public void updatePolicyNumberSetting(@PathVariable String id, @Valid @RequestBody PolicyNumberSetting policyNumberSetting) {
    	policyNumberSettingService.updateSetting(policyNumberSetting);
    }
}
