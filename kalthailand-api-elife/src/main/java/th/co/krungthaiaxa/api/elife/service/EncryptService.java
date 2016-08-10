package th.co.krungthaiaxa.api.elife.service;

import java.util.List;

import javax.inject.Inject;

import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

@Service
public class EncryptService {

    private final static Logger logger = LoggerFactory.getLogger(EncryptService.class);
    private final PolicyRepository policyRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Must be compatible between thai id (13) and registrationKey (?)
     * Not sure how long the registrationKey, but it not longer than 50 chars. And the encrypted text is never less than 50 chars.
     */
    private final int PLAIN_TEXT_MAX_SIZE = 50;

    @Inject
    public EncryptService(PolicyRepository policyRepository, PaymentRepository paymentRepository) {
        this.policyRepository = policyRepository;
        this.paymentRepository = paymentRepository;
    }

    public void encryptData() {
        List<Policy> policies = (List<Policy>) policyRepository.findAll();

        for (Policy policy : policies) {
            List<Insured> insureds = policy.getInsureds();
            for (Insured insured : insureds) {
                encryptRegistrationIdForPerson(insured.getPerson());
            }

            List<Coverage> coverages = policy.getCoverages();
            for (Coverage coverage : coverages) {
                List<CoverageBeneficiary> coverageBeneficiaries = coverage.getBeneficiaries();
                for (CoverageBeneficiary coverageBeneficiary : coverageBeneficiaries) {
                    encryptRegistrationIdForPerson(coverageBeneficiary.getPerson());
                }
            }

            List<Payment> payments = paymentRepository.findByPolicyId(policy.getPolicyId());
            for (Payment payment : payments) {
                if (shouldEncrypt(payment.getRegistrationKey())) {
                    payment.setRegistrationKey(EncryptUtil.encrypt(payment.getRegistrationKey()));
                }
            }
            paymentRepository.save(payments);
        }
        policyRepository.save(policies);
    }

    private void encryptRegistrationIdForPerson(Person person) {
        if (person == null) {
            logger.warn("The person is null, cannot encrypt it's registration Id");
            return;
        }
        List<Registration> registrations = person.getRegistrations();
        for (Registration registration : registrations) {
            if (shouldEncrypt(registration.getId())) {
                registration.setId(EncryptUtil.encrypt(registration.getId()));
            }
        }
    }

    private boolean shouldEncrypt(String text) {
        return !StringUtil.isBlank(text) || isPlainText(text);
    }

    private boolean isPlainText(String text) {
        return text.length() < PLAIN_TEXT_MAX_SIZE;
    }
}
