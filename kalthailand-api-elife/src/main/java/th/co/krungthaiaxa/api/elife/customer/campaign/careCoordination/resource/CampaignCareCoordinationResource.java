package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.resource;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.data.CareCoordinationEntity;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model.CareCoordinationForm;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.service.CampaignCareCoordinationService;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author tuong.le on 10/31/17.
 */
@RestController
public class CampaignCareCoordinationResource {

    private final CampaignCareCoordinationService campaignCareCoordinationService;

    @Autowired
    public CampaignCareCoordinationResource(CampaignCareCoordinationService campaignCareCoordinationService) {
        this.campaignCareCoordinationService = campaignCareCoordinationService;
    }

    @ApiOperation(value = "Create Customer data for 'Care Coordination Campaign'",
            notes = "Insert data of customer so that we can get for the 'Care Coordination Campaign'.")
    @RequestMapping(value = "/customer/campaign/care-coordination",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public CareCoordinationEntity createCustomerCareCoordination(
            @RequestBody final CareCoordinationForm careCoordinationForm) {
        return campaignCareCoordinationService.createCustomer(careCoordinationForm);
    }

    @ApiOperation(value = "Download Customer data for 'Care Coordination Campaign'")
    @RequestMapping(value = "/customer/campaign/care-coordination/report/download",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public void downloadReport(HttpServletResponse response) {
        byte[] bytes = campaignCareCoordinationService.exportAll();
        DownloadUtil.writeExcelBytesToResponseWithDateRollFileName(
                response, bytes, "KrungThaiAXA-Care-Coordination-Campaign");
    }
}
