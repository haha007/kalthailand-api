package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.PolicyNumber;
import th.co.krungthaiaxa.elife.api.exception.ElifeException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.elife.api.model.line.LinePayResponse;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductFactory;
import th.co.krungthaiaxa.elife.api.repository.*;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.*;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.RegistrationTypeName.THAI_ID_NUMBER;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.elife.api.service.LineService.LINE_PAY_INTERNAL_ERROR;

@Service
public class PolicyService {
    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);

    private final CDBRepository cdbRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNumberRepository policyNumberRepository;
    private final QuoteRepository quoteRepository;
    private final EmailService emailService;
    private final LineService lineService;
    private final DocumentService documentService;
    private final SMSApiService smsApiService;
    private final ProductFactory productFactory;

    @Inject
    public PolicyService(CDBRepository cdbRepository,
                         PaymentRepository paymentRepository,
                         PolicyRepository policyRepository,
                         PolicyNumberRepository policyNumberRepository,
                         QuoteRepository quoteRepository,
                         EmailService emailService,
                         LineService lineService, DocumentService documentService,
                         SMSApiService smsApiService,
                         ProductFactory productFactory) {
        this.cdbRepository = cdbRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyNumberRepository = policyNumberRepository;
        this.quoteRepository = quoteRepository;
        this.emailService = emailService;
        this.lineService = lineService;
        this.documentService = documentService;
        this.smsApiService = smsApiService;
        this.productFactory = productFactory;
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

            Product product = productFactory.getProduct(quote.getCommonData().getProductId());
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

    public void updatePayment(Payment payment, String orderId, String transactionId) {
        payment.setTransactionId(transactionId);
        payment.setOrderId(orderId);
        paymentRepository.save(payment);
        logger.info("Payment [" + payment.getPaymentId() + "] has been booked with transactionId [" + payment.getTransactionId() + "]");
    }

    public void updatePaymentWithErrorStatus(Payment payment, Double amount, String currencyCode, ChannelType channelType,
                                             String errorCode, String errorMessage) {
        updatePayment(payment, amount, currencyCode, channelType,
                errorCode,
                errorMessage,
                null,
                null,
                null);
    }

    public void updatePayment(Payment payment, Double amount, String currencyCode, ChannelType channelType,
                              LinePayResponse linePayResponse) {
        // Update the confirmed payment
        updatePayment(payment, amount, currencyCode, channelType,
                linePayResponse.getReturnCode(),
                linePayResponse.getReturnMessage(),
                linePayResponse.getInfo().getRegKey(),
                linePayResponse.getInfo().getPayInfo().get(0).getCreditCardName(),
                linePayResponse.getInfo().getPayInfo().get(0).getMethod());
    }

    public void updateRegistrationForAllNotProcessedPayment(Policy policy, String registrationKey) {
        // Save the registration key in all other payments
        for (Payment tmp : policy.getPayments()) {
            if (tmp.getStatus().equals(NOT_PROCESSED)) {
                updatePaymentRegistrationKey(tmp, registrationKey);
            }
        }
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

        // Send Email
        try {
            emailService.sendPolicyBookedEmail(policy);
        } catch (IOException | MessagingException e) {
            logger.error(String.format("Unable to send email for booking policy [%1$s].", policy.getPolicyId()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-booked-sms.txt"), Charset.forName("UTF-8"));
            Map<String, String> m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()));
            if (!m.get("STATUS").equals("0")) {
                logger.error(String.format("SMS for policy booking could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to send policy booking SMS message on policy [%1$s].", policy.getPolicyId()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/policy-booked-notification.txt"), Charset.forName("UTF-8"));
            lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), policy.getInsureds().get(0).getPerson().getLineId());
        } catch (IOException e) {
            logger.error(String.format("Unable to send push notification for policy booking on policy [%1$s].", policy.getPolicyId()), e);
        }

        policy.setStatus(PENDING_VALIDATION);
        policyRepository.save(policy);
    }

    public void updatePolicyAfterPolicyHasBeenValidated(Policy policy) {
        if (!policy.getStatus().equals(PENDING_VALIDATION)) {
            throw new ElifeException("Can't validate policy [" + policy.getPolicyId() + "], it is not pending for validation.");
        }

        // Generate documents
        documentService.generateValidatedPolicyDocuments(policy);

        // Get eReceipt
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (!documentPdf.isPresent()) {
            throw new ElifeException("Can't find eReceipt for the policy [" + policy.getPolicyId() + "]");
        }

        // Send Email
        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        try {
            emailService.sendEreceiptEmail(policy, Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policy.getPolicyId() + ".pdf"));
        } catch (IOException | MessagingException e) {
            logger.error(String.format("Unable to send email for validation of policy [%1$s].", policy.getPolicyId()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-purchased-sms.txt"), Charset.forName("UTF-8"));
            Map<String, String> m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()));
            if (!m.get("STATUS").equals("0")) {
                logger.error(String.format("SMS for policy validation could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to send policy validation SMS message on policy [%1$s].", policy.getPolicyId()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/policy-purchased-notification.txt"), Charset.forName("UTF-8"));
            lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), policy.getInsureds().get(0).getPerson().getLineId());
        } catch (IOException e) {
            logger.error(String.format("Unable to send push notification for policy validation on policy [%1$s].", policy.getPolicyId()), e);
        }

        policy.setStatus(VALIDATED);
        policyRepository.save(policy);
    }

    public void sendNotificationsWhenUserNotRespondingToCalls(Policy policy) throws IOException, MessagingException {
        // Send Email
        emailService.sendUserNotRespondingEmail(policy);

        // Send SMS
        String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/user-not-responging-sms.txt"), Charset.forName("UTF-8"));
        Map<String, String> m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()));
        if (!m.get("STATUS").equals("0")) {
            throw new IOException(String.format("SMS when user not responding could not be sent on policy [%1$s].", policy.getPolicyId()));
        }

        // Send push notification
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/user-not-responging-notification.txt"), Charset.forName("UTF-8"));
        lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), policy.getInsureds().get(0).getPerson().getLineId());
    }

    public void sendNotificationsWhenPhoneNumberIsWrong(Policy policy) throws IOException, MessagingException {
        // Send Email
        emailService.sendPhoneNumberIsWrongEmail(policy);

        // Send push notification
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/phone-number-wrong-notification.txt"), Charset.forName("UTF-8"));
        lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), policy.getInsureds().get(0).getPerson().getLineId());
    }

    private void updatePayment(Payment payment, Double value, String currencyCode, ChannelType channelType, String errorCode, String errorMessage, String registrationKey, String creditCardName, String paymentMethod) {
        SuccessErrorStatus status;
        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = ERROR;
            errorMessage = "Currencies are different";
            errorCode = LINE_PAY_INTERNAL_ERROR;
        } else if (!isEmpty(errorCode) && !errorCode.equals("0000")) {
            status = ERROR;
        } else {
            status = SUCCESS;
        }

        Amount amount = new Amount();
        amount.setCurrencyCode(currencyCode);
        amount.setValue(value);

        // registration key might have to be updated
        updatePaymentRegistrationKey(payment, registrationKey);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount);
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setDate(LocalDate.now(ZoneId.of(ZoneId.SHORT_IDS.get("VST"))));
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setRejectionErrorCode(errorCode);
        paymentInformation.setRejectionErrorMessage(errorMessage);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);

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

    private void updatePaymentRegistrationKey(Payment payment, String registrationKey) {
        if (!isBlank(registrationKey) && !registrationKey.equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey);
        }
    }
}
