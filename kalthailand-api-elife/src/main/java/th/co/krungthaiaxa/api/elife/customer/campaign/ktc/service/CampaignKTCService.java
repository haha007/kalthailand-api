package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.service;

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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tuong.le on 10/17/17.
 */
@Service
public class CampaignKTCService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignKTCService.class);
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

    //TODO: Make it as composite pattern
    protected CampaignKTCLine parseEntityToReportLine(final CampaignKTC campaignKTC) {
        CampaignKTCLine line = new CampaignKTCLine();
        line.setTitle(campaignKTC.getTitle());
        line.setName(campaignKTC.getName());
        line.setSurname(campaignKTC.getSurname());
        //TODO: format date
        //line.setDob(campaignKTC.getDob().toString());
        line.setIdCard(campaignKTC.getIdCard());
        line.setPhoneNumber(campaignKTC.getPhoneNumber());
        line.setBeneficiary(campaignKTC.getBeneficiary());
        return line;
    }
}
