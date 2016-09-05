package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionPlanService;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "Commission")
public class CommissionResource {
    private final static Logger logger = LoggerFactory.getLogger(CommissionResource.class);
    private final CommissionPlanService commissionPlanService;

    @Inject
    public CommissionResource(CommissionPlanService commissionPlanService) {this.commissionPlanService = commissionPlanService;}

    @ApiOperation(value = "Get all commission plans.", notes = "Get all commission plans. Each plan will represent how commission is divided for all partners.", response = CommissionPlan.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class)
    })
    @RequestMapping(value = "/commissions/plans", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public List<CommissionPlan> findAllCommissionPlans() {
        return commissionPlanService.findAll();
    }

    @ApiOperation(value = "Save a list of commission plans.", notes = "If a commission plan doesn't have Id, it will be added with a new generated id. If a commission plan has existing id, it will be updated. The result is the list of updated commission plans (with updated id,"
            + " createdDateTime and updatedDateTime", response =
            CommissionPlan.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there's any internal error", response = Error.class)
    })
    @RequestMapping(value = "/commissions/plans", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public List<CommissionPlan> saveCommissionsPlans(List<CommissionPlan> savingCommissionPlans) {
        return commissionPlanService.saveCommissions(savingCommissionPlans);
    }
}
