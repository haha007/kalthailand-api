package th.co.krungthaiaxa.api.elife.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.PreviousPolicy;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class CDBClient {
    private final static Logger logger = LoggerFactory.getLogger(CDBClient.class);
    @Inject
    private CDBRepository cdbRepository;

    public Optional<PreviousPolicy> getExistingAgentCode(String insuredRegistrationId, String insuredDOB) {
        return cdbRepository.findLastActivatingPreviousPolicy(insuredRegistrationId, insuredDOB);
    }

    public void setCdbRepository(CDBRepository cdbRepository) {
        this.cdbRepository = cdbRepository;
    }
}
