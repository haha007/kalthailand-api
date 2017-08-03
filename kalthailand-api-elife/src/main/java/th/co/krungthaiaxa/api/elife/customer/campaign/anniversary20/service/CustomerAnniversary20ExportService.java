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
    private static final String BKK_PROVINCE = "กรุงเทพมหานคร";
    private static final String PREFIX_DISTRICT_BKK_TH = "แขวง.";
    private static final String PREFIX_SUB_DISTRICT_BKK_TH = "เขต.";
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
        final String province = entity.getProvince();
        String provinceAndZipCode = StringUtils.EMPTY;// = entity.getProvince();
        String district = StringUtils.EMPTY;
        String subDistrict = StringUtils.EMPTY;
        if (StringUtils.isNotEmpty(province)) {
            provinceAndZipCode = PREFIX_PROVINCE_TH + province + " " + entity.getZipCode();
            if (BKK_PROVINCE.equalsIgnoreCase(province)) {
                district = PREFIX_DISTRICT_BKK_TH + entity.getDistrict();
                subDistrict = PREFIX_SUB_DISTRICT_BKK_TH + entity.getSubDistrict();
            } else {
                district = PREFIX_DISTRICT_TH + entity.getDistrict();
                subDistrict = PREFIX_SUB_DISTRICT_TH + entity.getSubDistrict();
            }
        }

        final CustomerAnniversary20Line reportLine = new CustomerAnniversary20Line();
        reportLine.setGivenName(entity.getFirstName());
        reportLine.setSurname(entity.getLastName());
        reportLine.setThaiId(entity.getThaiID());
        reportLine.setEmail(entity.getEmail());
        reportLine.setMobile(entity.getEmail());
        reportLine.setAddress(entity.getAddress());
        reportLine.setHomeNumber(entity.getHomeNumber());
        reportLine.setRoad(entity.getRoad());
        reportLine.setDistrict(district);
        reportLine.setSubDistrict(subDistrict);
        reportLine.setProvinceZipCode(provinceAndZipCode);
        reportLine.setPurchaseReason(entity.getPurchaseReason());
        reportLine.setForceChangeAddress(entity.isForceChangeAddress() ? "Y" : "N");
        return reportLine;
    }
}
