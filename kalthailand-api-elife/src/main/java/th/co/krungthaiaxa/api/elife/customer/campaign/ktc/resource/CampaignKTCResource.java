package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.resource;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.data.CampaignKTC;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model.CampaignKTCForm;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.service.CampaignKTCService;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author khoi.tran on 12/26/16.
 */
@RestController
public class CampaignKTCResource {

    private final CampaignKTCService campaignKTCService;

    @Autowired
    public CampaignKTCResource(CampaignKTCService campaignKTCService) {
        this.campaignKTCService = campaignKTCService;
    }

    @ApiOperation(value = "Create Customer data for 'KTC Campaign'",
            notes = "Insert data of customer so that we can do upselling for the 'KTC Campaign'.")
    @RequestMapping(value = "/customer/campaign/ktc",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public CampaignKTC createCustomerKTC(@RequestBody final CampaignKTCForm ktcForm) {
        return campaignKTCService.createKTCCustomer(ktcForm);
    }

    @ApiOperation(value = "Download Customer data for 'KTC Campaign'")
    @RequestMapping(value = "/customer/campaign/ktc/report/download",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public void downloadReport(HttpServletResponse response) {
        byte[] bytes = campaignKTCService.exportAll();
        DownloadUtil.writeExcelBytesToResponseWithDateRollFileName(response, bytes, "KrungThaiAXA-KTC-Campaign");
    }
}
