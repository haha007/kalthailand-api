package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.repository.PolicyRepository;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;

import javax.inject.Inject;

import static th.co.krungthaiaxa.ebiz.api.service.QuoteToPolicyUtils.getPolicyFromQuote;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;

    @Inject
    public PolicyService(PolicyRepository policyRepository, QuoteRepository quoteRepository) {
        this.policyRepository = policyRepository;
        this.quoteRepository = quoteRepository;
    }

    public Policy createPolicy(Quote quote) throws PolicyValidationException {
        if (quote == null) {
            throw PolicyValidationException.emptyQuote;
        } else if (quoteRepository.findOne(quote.getTechnicalId()) == null) {
            throw PolicyValidationException.noneExistingQuote;
        }

        Policy policy = policyRepository.findByQuoteFunctionalId(quote.getQuoteId());
        if (policy == null) {
            policy = new Policy();
            policy.setPolicyId(RandomStringUtils.randomNumeric(20));
            getPolicyFromQuote(policy, quote);
            policyRepository.save(policy);
        }

        return policy;
    }

}
