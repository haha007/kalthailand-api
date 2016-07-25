package th.co.krungthaiaxa.api.elife.resource;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.PolicyQuotaService;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

@RestController
@Api(value = "PoliciyQuota")
public class PolicyQuotaResource {
	
	private final static Logger logger = LoggerFactory.getLogger(PolicyQuotaResource.class);
	private final PolicyQuotaService policyQuotaService;
	
	@Inject
	public PolicyQuotaResource(PolicyQuotaService policyQuotaService){
		this.policyQuotaService = policyQuotaService;
	}

	@ApiOperation(value = "Get a list of policy quota data", notes = "Get a list of policy quota data.", response = PolicyQuota.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If policy quota data not exists", response = Error.class)
    })
    @RequestMapping(value = "/policy-quota", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
	public ResponseEntity<byte[]> getPolicyQuota(){
		
		PolicyQuota policyQuota = policyQuotaService.getPolicyQuota(null);
		List<PolicyQuota> listPolicyQuota = new ArrayList<>();
		listPolicyQuota.add(policyQuota);
		
		if(null==policyQuota){
			return new ResponseEntity<>(getJson(ErrorCode.POLICY_QUOTA_DOES_NOT_EXIST), NOT_FOUND);
		}else{
			policyQuota.setRowId(0);
			return new ResponseEntity<>(getJson(listPolicyQuota), OK);
		}
		
	}
	
	@ApiOperation(value = "Get policy quota data", notes = "Get a policy quota data.", response = PolicyQuota.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If policy quota data not exists", response = Error.class)
    })
    @RequestMapping(value = "/policy-quota/{rowId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
	public ResponseEntity<byte[]> getPolicyQuota(
			@ApiParam(value = "The policy quota rowId", required = true)
			@PathVariable String rowId){
		
		PolicyQuota policyQuota = policyQuotaService.getPolicyQuota(rowId);
		
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
    @RequestMapping(value = "/policy-quota/{rowId}", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
	public ResponseEntity<byte[]> updatePolicyQuota(
			@ApiParam(value = "The policy quota to update")
			@RequestBody String jsonPolicyQuota,
			@ApiParam(value = "The policy quota rowId", required = true)
			@PathVariable String rowId){
		
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
		
		policyQuotaService.updatePolicyQuota(policyQuota, rowId);
		return new ResponseEntity<>(getJson(""),OK);
		
	}
	
	@ApiOperation(value = "Upload policy number file", notes = "Uploads an Excel file (must be a xlsx file) containing the policy number.", response = PolicyNumber.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If Excel file is not in invalid format", response = Error.class)
    })
    @RequestMapping(value = "/policy-quota/upload", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadPolicyQuotaFileFile(
            @ApiParam(required = true, value = "The Excel file to upload")
            @RequestParam("file") MultipartFile file) {
        try {
            return new ResponseEntity<>(getJson(policyQuotaService.readPolicyNumberExcelFile(file.getInputStream())), CREATED);
        } catch (IOException | SAXException | OpenXML4JException | ParserConfigurationException | IllegalArgumentException | ElifeException e) {
            return new ResponseEntity<>(getJson(INVALID_POLICY_NUMBER_EXCEL_FILE), NOT_ACCEPTABLE);
        }
    }
    
    @ApiOperation(value = "List of policy number", notes = "Gets a list on policy number", response = PolicyNumber.class, responseContainer = "List")
    @RequestMapping(value = "/policy-quota/available", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> blackList(
            @ApiParam(required = true, value = "Page number (starts at 0)")
            @RequestParam Integer pageNumber,
            @ApiParam(required = true, value = "Number of elements per page")
            @RequestParam Integer pageSize) {
        return new ResponseEntity<>(getJson(policyQuotaService.findAll(pageNumber, pageSize)), OK);
    }
	
}
