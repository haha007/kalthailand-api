package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.resource;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model.CustomerAnniversary20Form;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.service.CustomerAnniversary20ExportService;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.service.CustomerAnniversary20Service;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author khoi.tran on 12/26/16.
 */
@RestController
public class CustomerAnniversary20Resource {

    private final CustomerAnniversary20Service customerAnniversary20Service;
    private final CustomerAnniversary20ExportService customerAnniversary20ExportService;

    @Autowired
    public CustomerAnniversary20Resource(CustomerAnniversary20Service customerAnniversary20Service,
                                         CustomerAnniversary20ExportService customerAnniversary20ExportService) {
        this.customerAnniversary20Service = customerAnniversary20Service;
        this.customerAnniversary20ExportService = customerAnniversary20ExportService;
    }

    @ApiOperation(value = "Create Customer data for 'Krungthai-AXA 20th Anniversary'",
            notes = "Insert data of customer so that we can do upselling for the 'Krungthai AXA 20th Anniversary' campaign.")
    @RequestMapping(value = "/customer/campaign/krungthai-axa-20th-anniversary",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public CustomerAnniversary20 createCustomer(@RequestBody final CustomerAnniversary20Form customerAnniversary20Form) {
        return customerAnniversary20Service.createCustomerAnniversary20(customerAnniversary20Form);
    }

    @ApiOperation(value = "Create Customer data for 'Krungthai-AXA 20th Anniversary'",
            notes = "Insert data of customer so that we can do upselling for the 'Krungthai AXA 20th Anniversary' campaign.")
    @RequestMapping(value = "/customer/campaign/krungthai-axa-20th-anniversary/report/download",
            produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public void downloadReport(HttpServletResponse response) {
        byte[] bytes = customerAnniversary20ExportService.exportAll();
        DownloadUtil.writeExcelBytesToResponseWithDateRollFileName(response, bytes, "KrungThaiAXA-20th-Anniverary");
    }
}
