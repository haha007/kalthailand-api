package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.repository.CustomerAnniversary20Repository;
import th.co.krungthaiaxa.api.elife.export.ExcelExportUtil;

import java.util.List;

/**
 * @author khoi.tran on 12/26/16.
 */
@Service
public class CustomerAnniversary20ExportService {

    private final CustomerAnniversary20Repository customerAnniversary20Repository;

    @Autowired
    public CustomerAnniversary20ExportService(CustomerAnniversary20Repository customerAnniversary20Repository) {this.customerAnniversary20Repository = customerAnniversary20Repository;}

    public byte[] exportAll() {
        List<CustomerAnniversary20> customerAnniversary20List = customerAnniversary20Repository.findAll();
        return ExcelExportUtil.exportObjectToRows(customerAnniversary20List);
    }
}
