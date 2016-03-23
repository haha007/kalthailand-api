package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.PolicyNumber;
import th.co.krungthaiaxa.elife.api.exception.ElifeException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.repository.*;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ofPattern;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.RegistrationTypeName.THAI_ID_NUMBER;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class PolicyService {
    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);

    private final CDBRepository cdbRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNumberRepository policyNumberRepository;
    private final QuoteRepository quoteRepository;
    private final EmailService emailService;
    private final DocumentService documentService;
    private final SMSApiService smsApiService;

    @Inject
    public PolicyService(CDBRepository cdbRepository,
                         PaymentRepository paymentRepository,
                         PolicyRepository policyRepository,
                         PolicyNumberRepository policyNumberRepository,
                         QuoteRepository quoteRepository,
                         EmailService emailService,
                         DocumentService documentService,
                         SMSApiService smsApiService) {
        this.cdbRepository = cdbRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyNumberRepository = policyNumberRepository;
        this.quoteRepository = quoteRepository;
        this.emailService = emailService;
        this.documentService = documentService;
        this.smsApiService = smsApiService;
    }

    public List<Policy> findAll(Integer startIndex, Integer nbOfRecords) {
        Page<Policy> policies = policyRepository.findAll(new PageRequest(startIndex, nbOfRecords));
        return policies != null ? policies.getContent() : new ArrayList<>();
    }

    public Optional<Policy> findPolicy(String policyId) {
        Policy policy = policyRepository.findByPolicyId(policyId);
        return policy != null ? Optional.of(policy) : Optional.empty();
    }

    public Policy createPolicy(Quote quote) {
        notNull(quote, emptyQuote);
        notNull(quote.getId(), noneExistingQuote);
        notNull(quoteRepository.findOne(quote.getId()), noneExistingQuote);

        Stream<PolicyNumber> availablePolicyNumbers = policyNumberRepository.findByPolicyNull();
        notNull(availablePolicyNumbers, noPolicyNumberAccessible);

        Optional<PolicyNumber> policyNumber = availablePolicyNumbers.sorted((p1, p2) -> p1.getPolicyId().compareTo(p2.getPolicyId())).findFirst();
        isTrue(policyNumber.isPresent(), noPolicyNumberAvailable);

        Policy policy = policyRepository.findByQuoteId(quote.getId());
        if (policy == null) {
            logger.info("Creating Policy from quote [" + quote.getQuoteId() + "]");
            policy = new Policy();
            policy.setPolicyId(policyNumber.get().getPolicyId());

            Product product = ProductFactory.getProduct(quote.getCommonData().getProductId());
            product.getPolicyFromQuote(policy, quote);

            policy.getPayments().stream().forEach(paymentRepository::save);
            policy.setStatus(PENDING_PAYMENT);
            policy = policyRepository.save(policy);
            policyNumber.get().setPolicy(policy);
            policyNumberRepository.save(policyNumber.get());
            quote.setPolicyId(policy.getPolicyId());
            quoteRepository.save(quote);
            logger.info("Policy has been created with id [" + policy.getPolicyId() + "] from quote [" + quote.getQuoteId() + "]");
        }

        return policy;
    }

    public void updatePayment(Payment payment, Double value, String currencyCode,
                              Optional<String> registrationKey, SuccessErrorStatus status, ChannelType channelType,
                              Optional<String> creditCardName, Optional<String> paymentMethod,
                              Optional<String> errorCode, Optional<String> errorMessage) {
        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = ERROR;
            errorMessage = Optional.of("Currencies are different");
        }

        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount);
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName.isPresent() ? creditCardName.get() : null);
        paymentInformation.setDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        paymentInformation.setMethod(paymentMethod.isPresent() ? paymentMethod.get() : null);
        paymentInformation.setRejectionErrorCode(errorCode.isPresent() ? errorCode.get() : null);
        paymentInformation.setRejectionErrorMessage(errorMessage.isPresent() ? errorMessage.get() : null);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);
        if (registrationKey.isPresent() && !registrationKey.get().equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey.get());
        }

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SUCCESS))
                .mapToDouble(tmp -> tmp.getAmount().getValue())
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(INCOMPLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(COMPLETED);
            payment.setEffectiveDate(paymentInformation.getDate());
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(OVERPAID);
            payment.setEffectiveDate(paymentInformation.getDate());
        }

        paymentRepository.save(payment);
        logger.info("Payment [" + payment.getPaymentId() + "] has been updated");
    }

    public void updatePolicyAfterFirstPaymentValidated(Policy policy) {
        // Generate documents
        documentService.generateNotValidatedPolicyDocuments(policy);

        // Update the policy
        Optional<Registration> insuredId = policy.getInsureds().get(0).getPerson().getRegistrations()
                .stream()
                .filter(registration -> registration.getTypeName().equals(THAI_ID_NUMBER))
                .findFirst();
        String insuredDOB = policy.getInsureds().get(0).getPerson().getBirthDate().format(ofPattern("yyyyMMdd"));
        if (insuredId.isPresent()) {
            Optional<Triple<String, String, String>> agent = cdbRepository.getExistingAgentCode(insuredId.get().getId(), insuredDOB);
            if (agent.isPresent()) {
                String agent1 = agent.get().getMiddle();
                if (agent1 != null) {
                    policy.getInsureds().get(0).addInsuredPreviousAgent(agent1);
                }

                String agent2 = agent.get().getRight();
                if (agent2 != null) {
                    policy.getInsureds().get(0).addInsuredPreviousAgent(agent2);
                }
            }
        }

        policy.setStatus(PENDING_VALIDATION);
        policyRepository.save(policy);
    }

    public void updatePolicyAfterPolicyHasBeenValidated(Policy policy, Document ereceiptPdf) {
        // Generate documents
        documentService.generateValidatedPolicyDocuments(policy);

        // Send Email
        DocumentDownload documentDownload = documentService.downloadDocument(ereceiptPdf.getId());
        try {
            emailService.sendEreceiptEmail(policy, Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policy.getPolicyId() + ".pdf"));
        } catch (IOException | MessagingException e) {
            logger.error(String.format("Unable to send e-receipt document while sending email with policy id is [%1$s].", policy.getPolicyId()), e);
        }

        // Send SMS
        try {
            Map<String,String> m = smsApiService.sendConfirmationMessage(policy);
            if(!m.get("STATUS").equals("0")){
                //return new ResponseEntity<>(SMS_IS_UNAVAILABLE, INTERNAL_SERVER_ERROR);
                throw new ElifeException();
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to send confirmation SMS message with policy id is [%1$s].", policy.getPolicyId()), e);
            //return new ResponseEntity<>(UNABLE_TO_SEND_SMS, INTERNAL_SERVER_ERROR);
            throw new ElifeException("Unexpected error",e);
        }

        policy.setStatus(VALIDATED);
        policyRepository.save(policy);
    }
}
