package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.data.CampaignKTC;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model.CampaignKTCForm;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model.CampaignKTCLine;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.repository.CampaignKTCRepository;
import th.co.krungthaiaxa.api.elife.export.ExcelExportUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author tuong.le on 10/17/17.
 */
@Service
public class CampaignKTCService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignKTCService.class);
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    private final BeanValidator beanValidator;
    private final CampaignKTCRepository campaignKTCRepository;

    @Autowired
    public CampaignKTCService(BeanValidator beanValidator,
                              CampaignKTCRepository campaignKTCRepository) {
        this.beanValidator = beanValidator;
        this.campaignKTCRepository = campaignKTCRepository;
    }

    public CampaignKTC createKTCCustomer(final CampaignKTCForm ktcForm) {
        beanValidator.validate(ktcForm);
        final CampaignKTC entity = new CampaignKTC(ktcForm);
        return campaignKTCRepository.save(entity);
    }

    public byte[] exportAll() {
        final List<CampaignKTC> customerKTCs = campaignKTCRepository.findAll();
        final List<CampaignKTCLine> exportLines =
                customerKTCs.stream().map(this::parseEntityToReportLine).collect(Collectors.toList());
        LOGGER.info("Exported {} records for Campaign KTC ", exportLines.size());
        return ExcelExportUtil.exportObjectToRows(exportLines);
    }

    private CampaignKTCLine parseEntityToReportLine(final CampaignKTC campaignKTC) {
        CampaignKTCLine line = new CampaignKTCLine();
        line.setGender(campaignKTC.getGender());
        line.setName(campaignKTC.getName());
        final LocalDate dobLocalDate = campaignKTC.getDob();
        line.setDob(Objects.isNull(dobLocalDate)
                ? StringUtils.EMPTY : dobLocalDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        line.setSubmittedDate(campaignKTC.getCreatedDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        line.setIdCard(campaignKTC.getIdCard());
        line.setPhoneNumber(campaignKTC.getPhoneNumber());
        line.setBeneficiary(campaignKTC.getBeneficiary());
        return line;
    }
}
