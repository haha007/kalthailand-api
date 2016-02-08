package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Payment;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.products.Product10EC;
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

    public Policy findPolicy(String policyId) throws Exception {
        return policyRepository.findOne(policyId);
    }

    public Policy createPolicy(Quote quote) throws Exception {
        if (quote == null) {
            throw PolicyValidationException.emptyQuote;
        } else if (quote.getTechnicalId() == null || quoteRepository.findOne(quote.getTechnicalId()) == null) {
            throw PolicyValidationException.noneExistingQuote;
        }

        Policy policy = policyRepository.findByQuoteFunctionalId(quote.getTechnicalId());
        if (policy == null) {
            policy = new Policy();
            policy.setPolicyId(RandomStringUtils.randomNumeric(20));
            // Only one product so far
            Product10EC.getPolicyFromQuote(policy, quote);
            policy = policyRepository.save(policy);
        }

        return policy;
    }

    public void addPayment(String policyId, Double value, String cuurrencyCode) {
        Policy policy = policyRepository.findOne(policyId);

        Payment payment = new Payment();
//        payment.setAmount();
//        payment.setEffectiveDate();
//        payment.setPaymentInformations();
//        payment.setRegistrationKey();
//        payment.setStatus();
        policy.addPayment(payment);
    }
}
