package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.repository.PolicyRepository;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;

import javax.inject.Inject;

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
            throw PolicyValidationException.policyCantBeCreatedFromEmptyQuoteException;
        } else if (quoteRepository.findOne(quote.getTechnicalId()) == null) {
            throw PolicyValidationException.policyCantBeCreatedFromNoneExistingQuoteException;
        }

        Policy policy = policyRepository.findByQuoteFunctionalId(quote.getQuoteId());
        if (policy == null) {
            policy = new Policy();
            policy.setPolicyId(RandomStringUtils.randomNumeric(20));
            fillUpDateFromQuote(policy, quote);
            policyRepository.save(policy);
        }

        return policy;
    }

    public static void fillUpDateFromQuote(Policy policy, Quote quote) throws PolicyValidationException {
        policy.setQuoteFunctionalId(quote.getQuoteId());
        policy.setCommonData(SerializationUtils.clone(quote.getCommonData()));
        quote.getCoverages().stream().forEach(coverage -> policy.addCoverage(SerializationUtils.clone(coverage)));
        quote.getInsureds().stream().forEach(insured -> policy.addInsured(SerializationUtils.clone(insured)));
        policy.setPremiumsData(SerializationUtils.clone(quote.getPremiumsData()));
    }
}
