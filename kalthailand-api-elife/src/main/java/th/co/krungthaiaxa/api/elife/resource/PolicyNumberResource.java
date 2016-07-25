package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.*;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.service.PolicyQuotaService;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@RestController
@Api(value = "PolicyNumber")
public class PolicyNumberResource {

    private final static Logger logger = LoggerFactory.getLogger(PolicyNumberResource.class);
    private final PolicyQuotaService policyQuotaService;

    @Inject
    public PolicyNumberResource(PolicyQuotaService policyQuotaService) {
        this.policyQuotaService = policyQuotaService;
    }

    @ApiOperation(value = "Upload policy number file", notes = "Uploads an Excel file (must be a xlsx file) containing the policy number.", response = PolicyNumber.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If Excel file is not in invalid format", response = Error.class)
    })
    @RequestMapping(value = "/policy-number/upload", produces = APPLICATION_JSON_VALUE, method = POST)
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
    @RequestMapping(value = "/policy-number/available", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAvailablePolicyNumbers(
            @ApiParam(required = true, value = "Page number (starts at 0)")
            @RequestParam Integer pageNumber,
            @ApiParam(required = true, value = "Number of elements per page")
            @RequestParam Integer pageSize) {
        return new ResponseEntity<>(getJson(policyQuotaService.findAll(pageNumber, pageSize)), OK);
    }

}
