package th.co.krungthaiaxa.api.elife.resource;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.PolicyQuotaService;
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
			return new ResponseEntity<>(getJson(policyQuota), OK);
		}
	}
	
}
