package th.co.krungthaiaxa.api.elife.client;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.repository.CDBRepository;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class CDBClient {
    private final static Logger logger = LoggerFactory.getLogger(CDBClient.class);
    @Inject
    private CDBRepository cdbRepository;

    public Optional<Triple<String, String, String>> getExistingAgentCode(String id, String insuredDOB) {
        logger.info("Connecting to CDB");
        return cdbRepository.getExistingAgentCode(id, insuredDOB);
    }

    public void setCdbRepository(CDBRepository cdbRepository) {
        this.cdbRepository = cdbRepository;
    }
}
