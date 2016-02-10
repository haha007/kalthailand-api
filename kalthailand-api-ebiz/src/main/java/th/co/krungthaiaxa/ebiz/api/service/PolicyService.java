package th.co.krungthaiaxa.ebiz.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.*;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.model.enums.PaymentStatus;
import th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.ebiz.api.products.Product10EC;
import th.co.krungthaiaxa.ebiz.api.repository.PaymentRepository;
import th.co.krungthaiaxa.ebiz.api.repository.PolicyRepository;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.SUCCESS;

@Service
public class PolicyService {
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;
    @Value("policy.number.prefix")
    private String policyNumberPrefix;

    @Inject
    public PolicyService(PaymentRepository paymentRepository, PolicyRepository policyRepository, QuoteRepository quoteRepository) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.quoteRepository = quoteRepository;
    }

    public Policy findPolicy(String policyId) {
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
            policy.setPolicyId(policyNumberPrefix + RandomStringUtils.randomNumeric(20));
            // Only one product so far
            Product10EC.getPolicyFromQuote(policy, quote);
            policy.getPayments().stream().forEach(paymentRepository::save);
            policy = policyRepository.save(policy);
        }

        return policy;
    }

    public Payment updatePayment(String paymentId, Double value, String currencyCode, String registrationKey,
                                 SuccessErrorStatus status, ChannelType channelType, String creditCardName,
                                 String paymentMethod, String errorMessage) {
        Payment payment = paymentRepository.findOne(paymentId);

        if (payment.getPaymentInformations() == null) {
            payment.setPaymentInformations(new ArrayList<>());
        }

        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = ERROR;
            errorMessage = "Currencies are different";
        }

        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount);
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setRejectionErrorMessage(errorMessage);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);
        if (registrationKey != null && !registrationKey.equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey);
        }

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SUCCESS))
                .mapToDouble(tmp -> tmp.getAmount().getValue())
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.INCOMLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setEffectiveDate(paymentInformation.getDate());
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(PaymentStatus.OVERPAID);
            payment.setEffectiveDate(paymentInformation.getDate());
        }

        return paymentRepository.save(payment);
    }
}
