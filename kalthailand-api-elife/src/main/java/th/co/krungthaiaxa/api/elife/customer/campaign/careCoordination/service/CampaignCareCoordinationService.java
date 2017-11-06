package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.data.CareCoordinationEntity;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model.CareCoordinationForm;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model.CareCoordinationLine;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.repository.CareCoordinationRepository;
import th.co.krungthaiaxa.api.elife.export.ExcelExportUtil;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tuong.le on 10/31/17.
 */
@Service
public class CampaignCareCoordinationService {
    private final BeanValidator beanValidator;
    private final CareCoordinationRepository careCoordinationRepository;

    @Inject
    public CampaignCareCoordinationService(BeanValidator beanValidator,
                                           CareCoordinationRepository careCoordinationRepository) {
        this.beanValidator = beanValidator;
        this.careCoordinationRepository = careCoordinationRepository;
    }

    public CareCoordinationEntity createCustomer(final CareCoordinationForm careCoordinationForm) {
        beanValidator.validate(careCoordinationForm);
        return careCoordinationRepository.save(new CareCoordinationEntity(careCoordinationForm));
    }

    public byte[] exportAll() {
        final List<CareCoordinationEntity> customers = careCoordinationRepository.findAll();
        final List<CareCoordinationLine> exportLines = customers.stream().map(this::parseEntityToReportLine)
                .collect(Collectors.toList());
        return ExcelExportUtil.exportObjectToRows(exportLines);
    }

    private CareCoordinationLine parseEntityToReportLine(final CareCoordinationEntity entity) {
        CareCoordinationLine line = new CareCoordinationLine();
        line.setName(entity.getName());
        line.setPolicyId(entity.getPolicyId());
        line.setPhoneNumber(entity.getPhoneNumber());
        line.setEmail(entity.getEmail());
        line.setSubmittedDate(entity.getCreatedDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        return line;
    }

}
