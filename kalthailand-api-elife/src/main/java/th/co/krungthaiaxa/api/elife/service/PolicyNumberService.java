package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberRepository;

import javax.inject.Inject;

import static org.springframework.util.Assert.notNull;

@Service
public class PolicyNumberService {

    private final static Logger logger = LoggerFactory.getLogger(PolicyNumberService.class);

    @Inject
    private PolicyNumberRepository policyNumberRepository;

    public long countAvailablePolicyNumbers() {
        return policyNumberRepository.countByPolicyNull();
    }

    public long countAllPolicyNumbers() {
        return policyNumberRepository.count();
    }
}
