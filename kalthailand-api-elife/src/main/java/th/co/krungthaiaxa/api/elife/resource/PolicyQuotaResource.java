package th.co.krungthaiaxa.api.elife.resource;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.PolicyQuotaService;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Api(value = "PoliciyQuota")
public class PolicyQuotaResource {
	
	private final static Logger logger = LoggerFactory.getLogger(PolicyQuotaResource.class);
	private final PolicyQuotaService policyQuotaService;
	
	@Inject
	public PolicyQuotaResource(PolicyQuotaService policyQuotaService){
		this.policyQuotaService = policyQuotaService;
	}

	@ApiOperation(value = "Get policy quota data", notes = "Get a policy quota data.", response = PolicyQuota.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If policy quota data not exists", response = Error.class)
    })
    @RequestMapping(value = "/policyquota", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
	public ResponseEntity<byte[]> getPolicyQuota(){
		PolicyQuota policyQuota = policyQuotaService.getPolicyQuota();
		if(null==policyQuota){
			return new ResponseEntity<>(getJson(ErrorCode.POLICY_QUOTA_DOES_NOT_EXIST), NOT_FOUND);
		}else{
			policyQuota.setRowId(0);
			return new ResponseEntity<>(getJson(policyQuota), OK);
		}
	}
	
	@ApiOperation(value = "Update a policy quota", notes = "Update a policy quota", response = Boolean.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If JSon of policy quota is invalid or if policy quota could not be update",
                    response = Error.class)
    })
    @RequestMapping(value = "/policyquota/update", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
	public ResponseEntity<byte[]> updatePolicyQuota(
			@ApiParam(value = "The policy quota to update")
			@RequestBody String jsonPolicyQuota){
		
		PolicyQuota policyQuota;
		
		try{
			policyQuota = JsonUtil.mapper.readValue(jsonPolicyQuota, PolicyQuota.class);
		}catch (IOException e) {
            logger.error("Unable to get a policy quota out of [" + jsonPolicyQuota + "]", e);
            return new ResponseEntity<>(getJson(INVALID_POLICY_QUOTA_PROVIDED), NOT_ACCEPTABLE);
        }
		
		if(policyQuota.getEmailList().size()<1){
			return new ResponseEntity<>(getJson(INVALID_POLICY_QUOTA_EMAIL_LIST), NOT_ACCEPTABLE);
		}
		
		if(policyQuota.getPercent()<1){
			return new ResponseEntity<>(getJson(INVALID_POLICY_QUOTA_PERCENT), NOT_ACCEPTABLE);
		}
		
		policyQuotaService.updatePolicyQuota(policyQuota);
		return new ResponseEntity<>(getJson(true),OK);
		
	}
	
}
