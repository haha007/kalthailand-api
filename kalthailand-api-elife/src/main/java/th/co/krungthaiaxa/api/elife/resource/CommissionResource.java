package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.common.utils.MimeTypeUtil;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionResult;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionCalculationSessionExportService;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionCalculationSessionService;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionPlanService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "Commission")
public class CommissionResource {
    private final static Logger logger = LoggerFactory.getLogger(CommissionResource.class);
    private final CommissionPlanService commissionPlanService;
    private final CommissionCalculationSessionService commissionCalculationSessionService;
    private final CommissionCalculationSessionExportService commissionCalculationSessionExportService;

    @Inject
    public CommissionResource(CommissionPlanService commissionPlanService, CommissionCalculationSessionService commissionCalculationSessionService, CommissionCalculationSessionExportService commissionCalculationSessionExportService) {
        this.commissionPlanService = commissionPlanService;
        this.commissionCalculationSessionService = commissionCalculationSessionService;
        this.commissionCalculationSessionExportService = commissionCalculationSessionExportService;
    }

    @ApiOperation(value = "Get all commission plans.", notes = "Get all commission plans. Each plan will represent how commission is divided for all partners.", response = CommissionPlan.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class) })
    @RequestMapping(value = "/commissions/plans", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<CommissionPlan> findAllCommissionPlans() {
        return commissionPlanService.findAll();
    }

    @ApiOperation(value = "Save a list of commission plans.", notes = "If a commission plan doesn't have Id, it will be added with a new generated id. If a commission plan has existing id, it will be updated. The result is the list of updated commission plans (with updated id, createdDateTime and updatedDateTime", response = CommissionPlan.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class) })
    @RequestMapping(value = "/commissions/plans", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public List<CommissionPlan> saveCommissionsPlans(@RequestBody List<CommissionPlan> savingCommissionPlans) {
        return commissionPlanService.putCommissions(savingCommissionPlans);
    }

    //santi : for trigger calculation commission
    @ApiOperation(value = "Calculate commission for policies.", notes = "Calculate commission for input policies based on commission plans.", response = CommissionCalculationSession.class)
    @ApiResponses({ @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class) })
    @RequestMapping(value = "/commissions/calculation", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public void calculateCommissionsForPolicies() {
        commissionCalculationSessionService.calculateCommissionForPolicies();
    }

    //santi : for get list of calculated commission
    @ApiOperation(value = "Get list of calculated commission transactions", notes = "Get list of calculated commission transactions")
    @ApiResponses({ @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class) })
    @RequestMapping(value = "/commissions/calculation/lists", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<CommissionCalculationSession> getCalculateCommissionsList() {
        return commissionCalculationSessionService.findAllCommissionCalculationSessions();
    }

//    //santi : for download commission excel file
//    @ApiOperation(value = "Commission download excel file", notes = "Commission download excel file", response = CommissionResult.class, responseContainer = "List")
//    @RequestMapping(value = "/commissions/calculation/download/{rowId}", method = GET)
//    @ResponseBody
//    public void getCommissionResultExcelFile(
//            @ApiParam(value = "The Commission Result Row Id", required = true)
//            @PathVariable String rowId,
//            HttpServletResponse response) {
//
//        String now = ofPattern("yyyyMMdd_HHmmss").format(now());
//        byte[] excelFileContent = commissionCalculationSessionExportService.exportToExcel(rowId, now);
//        DownloadUtil.writeBytesToResponse(response, excelFileContent, "commission_" + DateTimeUtil.formatNowForFilePath() + "_" + rowId, MimeTypeUtil.MIME_TYPE_PDF);
//    }

    @ApiOperation(value = "Download Commission Calculation Session")
    @RequestMapping(value = "/commissions/calculation-session/download/{calculation-session-id}", method = GET)
    public void downloadCommissionCalcuationSession(@PathVariable("calculation-session-id") String commissionCalculationSessionIdString, HttpServletResponse response) {
        logger.info("Downloading deduction File");
        byte[] excelFileContent = commissionCalculationSessionExportService.exportToExcel(commissionCalculationSessionIdString);
        DownloadUtil.writeBytesToResponse(response, excelFileContent, "commission_" + DateTimeUtil.formatNowForFilePath() + "_" + commissionCalculationSessionIdString, MimeTypeUtil.MIME_TYPE_XLSX);
    }
}
