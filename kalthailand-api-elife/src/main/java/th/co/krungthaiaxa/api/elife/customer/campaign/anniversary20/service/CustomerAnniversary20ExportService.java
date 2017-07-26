package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model.CustomerAnniversary20Line;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.repository.CustomerAnniversary20Repository;
import th.co.krungthaiaxa.api.elife.export.ExcelExportUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 12/26/16.
 */
@Service
public class CustomerAnniversary20ExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerAnniversary20ExportService.class);

    //these prefix are used for TH only
    private static final String PREFIX_DISTRICT_TH = "ต.";
    private static final String PREFIX_SUB_DISTRICT_TH = "อ.";
    private static final String PREFIX_PROVINCE_TH = "จ.";
    private final CustomerAnniversary20Repository customerAnniversary20Repository;

    @Autowired
    public CustomerAnniversary20ExportService(CustomerAnniversary20Repository customerAnniversary20Repository) {
        this.customerAnniversary20Repository = customerAnniversary20Repository;
    }

    public byte[] exportAll() {
        List<CustomerAnniversary20> customerAnniversary20List = customerAnniversary20Repository.findAll();
        List<CustomerAnniversary20Line> reportLines = customerAnniversary20List
                .stream()
                .map(this::parseEntityToReportLine)
                .collect(Collectors.toList());
        LOGGER.info("Exported {} records for Campaign 20ht Anniversary", reportLines.size());
        return ExcelExportUtil.exportObjectToRows(reportLines);
    }

    private CustomerAnniversary20Line parseEntityToReportLine(final CustomerAnniversary20 entity) {
        final CustomerAnniversary20Line reportLine = new CustomerAnniversary20Line();
        reportLine.setGivenName(entity.getFirstName());
        reportLine.setSurname(entity.getLastName());
        reportLine.setThaiId(entity.getThaiID());
        reportLine.setEmail(entity.getEmail());
        reportLine.setMobile(entity.getEmail());
        reportLine.setAddress(entity.getAddress());
        reportLine.setHomeNumber(entity.getHomeNumber());
        reportLine.setRoad(entity.getRoad());
        reportLine.setDistrict(Objects.isNull(entity.getDistrict())
                ? StringUtils.EMPTY : PREFIX_DISTRICT_TH + entity.getDistrict());
        reportLine.setSubDistrict(Objects.isNull(entity.getSubDistrict())
                ? StringUtils.EMPTY : PREFIX_SUB_DISTRICT_TH + entity.getSubDistrict());
        reportLine.setProvinceZipCode(Objects.isNull(entity.getProvince())
                ? StringUtils.EMPTY : PREFIX_PROVINCE_TH + entity.getProvince() + " " + entity.getZipCode());
        reportLine.setPurchaseReason(entity.getPurchaseReason());
        reportLine.setForceChange(entity.isForceChange() ? "Y" : "N");
        return reportLine;
    }
}
