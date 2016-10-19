package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.client.CDBClient;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyNotFoundException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;
import th.co.krungthaiaxa.api.elife.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductServiceFactory;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyCriteriaRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM_VALIDATED;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.DA_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.COMPLETED;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.INCOMPLETE;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.NOT_PROCESSED;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.OVERPAID;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.VALIDATED;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amount;

@Service
public class PolicyService {
    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);

    private final TMCClient tmcClient;
    private final PaymentRepository paymentRepository;
    private final PolicyCriteriaRepository policyCriteriaRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNumberRepository policyNumberRepository;
    private final QuoteRepository quoteRepository;
    private final EmailService emailService;
    private final LineService lineService;
    private final DocumentService documentService;
    private final SMSApiService smsApiService;
    private final ProductServiceFactory productServiceFactory;
    private final CDBClient cdbClient;

    @Inject
    private PolicyNumberSettingService policyNumberSettingService;

    @Inject
    public PolicyService(TMCClient tmcClient,
            PaymentRepository paymentRepository,
            PolicyCriteriaRepository policyCriteriaRepository,
            PolicyRepository policyRepository,
            PolicyNumberRepository policyNumberRepository,
            QuoteRepository quoteRepository,
            EmailService emailService,
            LineService lineService, DocumentService documentService,
            SMSApiService smsApiService,
            ProductServiceFactory productServiceFactory, CDBClient cdbClient) {
        this.tmcClient = tmcClient;
        this.cdbClient = cdbClient;
        this.paymentRepository = paymentRepository;
        this.policyCriteriaRepository = policyCriteriaRepository;
        this.policyRepository = policyRepository;
        this.policyNumberRepository = policyNumberRepository;
        this.quoteRepository = quoteRepository;
        this.emailService = emailService;
        this.lineService = lineService;
        this.documentService = documentService;
        this.smsApiService = smsApiService;
        this.productServiceFactory = productServiceFactory;
    }

    public Page<Policy> findAll(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate startDate,
            LocalDate endDate, Integer startIndex, Integer nbOfRecords) {
        return policyCriteriaRepository.findPolicies(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, new PageRequest(startIndex, nbOfRecords, new Sort(Sort.Direction.DESC, "policyId")));
    }

    public List<Policy> findAll(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate startDate,
            LocalDate endDate) {
        return policyCriteriaRepository.findPolicies(policyId, productType, status, nonEmptyAgentCode, startDate, endDate);
    }

    public Optional<Policy> findPolicyByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        return policy != null ? Optional.of(policy) : Optional.empty();
    }

    public Policy validateExistPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        if (policy == null) {
            throw new PolicyNotFoundException("Not found policy " + policyNumber);
        }
        return policy;
    }

    private PolicyNumber validateExistNextAvailablePoliyNumber() {
        Sort sort = new Sort(Sort.Direction.ASC, "policyId");
        Pageable pageable = new PageRequest(0, 1, sort);
        Page<PolicyNumber> policyNumbers = policyNumberRepository.findByPolicyNull(pageable);
        if (!policyNumbers.iterator().hasNext()) {
            throw PolicyValidationException.noPolicyNumberAvailable;
        }
        return policyNumbers.iterator().next();
    }

    public Policy createPolicy(Quote quote) {
        notNull(quote, PolicyValidationException.emptyQuote);
        notNull(quote.getId(), PolicyValidationException.noneExistingQuote);
        notNull(quoteRepository.findOne(quote.getId()), PolicyValidationException.noneExistingQuote);

        PolicyNumber policyNumber = validateExistNextAvailablePoliyNumber();

        Policy policy = policyRepository.findByQuoteId(quote.getId());
        if (policy == null) {
            logger.info("Creating Policy from quote [" + quote.getQuoteId() + "]");
            policy = new Policy();
            policy.setPolicyId(policyNumber.getPolicyId());

            ProductService productService = productServiceFactory.getProductService(quote.getCommonData().getProductId());
            productService.createPolicyFromQuote(policy, quote);

            policy.getPayments().stream().forEach(paymentRepository::save);
            policy.setStatus(PolicyStatus.PENDING_PAYMENT);
            Instant now = Instant.now();
            policy.setCreationDateTime(now);
            policy.setLastUpdateDateTime(now);
            policy = policyRepository.save(policy);
            policyNumber.setPolicy(policy);
            policyNumberRepository.save(policyNumber);
            quote.setPolicyId(policy.getPolicyId());
            quoteRepository.save(quote);
            logger.info("Policy has been created with id [" + policy.getPolicyId() + "] from quote [" + quote.getQuoteId() + "]");
        }

        return policy;
    }

    public Payment updatePayment(Payment payment, String orderId, String transactionId, String regKey) {
        payment.setTransactionId(transactionId);
        payment.setOrderId(orderId);
        payment.setRegistrationKey(regKey);
        Payment result = paymentRepository.save(payment);
        logger.info("Payment [" + payment.getPaymentId() + "] has been booked with transactionId [" + payment.getTransactionId() + "]");
        return result;
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
        String creditCardName = null;
        String method = null;
        if (linePayResponse.getInfo().getPayInfo().size() > 0) {
            creditCardName = linePayResponse.getInfo().getPayInfo().get(0).getCreditCardName();
            method = linePayResponse.getInfo().getPayInfo().get(0).getMethod();
        }
        // Update the confirmed payment
        updatePayment(payment, amount, currencyCode, channelType,
                linePayResponse.getReturnCode(),
                linePayResponse.getReturnMessage(),
                linePayResponse.getInfo().getRegKey(),
                creditCardName,
                method);
    }

    public void updateRegKeyForAllNotProcessedPayments(Policy policy, String newRegistrationKey) {
        if (isBlank(newRegistrationKey)) {
            return;
        }

        //TODO Improve performance: use Mongo query to update
        // Save the registration key in all other payments
        policy.getPayments().stream().filter(paymentPredicate -> paymentPredicate.getStatus().equals(NOT_PROCESSED)).forEach(payment -> {
            if (!newRegistrationKey.equals(payment.getRegistrationKey())) {
                payment.setRegistrationKey(newRegistrationKey);
            }
        });
    }

    public void updatePolicyAfterFirstPaymentValidated(Policy policy) {
        // Generate documents
        try {
            documentService.generateNotValidatedPolicyDocuments(policy);
        } catch (Exception e) {
            throw new ElifeException("Can't generate documents for the policy [" + policy.getPolicyId() + "]");
        }

        // Should block if Application form is not generated
        Optional<Document> applicationFormPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        if (!applicationFormPdf.isPresent()) {
            throw new ElifeException("Can't find Application form for the policy [" + policy.getPolicyId() + "]");
        }

        // Should block if DA form is not generated when Policy is monthly payment
        Optional<Document> daFormPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(DA_FORM)).findFirst();
        if (!daFormPdf.isPresent() && policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            throw new ElifeException("Can't find DA form for the policy [" + policy.getPolicyId() + "] while it is mandatory for Monthly Policy");
        }

        // Update the policy
        Optional<Registration> insuredId = policy.getInsureds().get(0).getPerson().getRegistrations()
                .stream()
                .filter(registration -> registration.getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER))
                .findFirst();
        String insuredDOB = policy.getInsureds().get(0).getPerson().getBirthDate().format(ofPattern("yyyyMMdd"));
        if (insuredId.isPresent()) {
            Optional<Triple<String, String, String>> agent = cdbClient.getExistingAgentCode(insuredId.get().getId(), insuredDOB);
            if (agent.isPresent()) {
                String previousPolicy = agent.get().getLeft();
                String agent1 = agent.get().getMiddle();
                String agent2 = agent.get().getRight();
                policy.getInsureds().get(0).addInsuredPreviousInformation((previousPolicy != null) ? previousPolicy : "NULL");
                policy.getInsureds().get(0).addInsuredPreviousInformation((agent1 != null) ? agent1 : "NULL");
                policy.getInsureds().get(0).addInsuredPreviousInformation((agent2 != null) ? agent2 : "NULL");
            }
        }

        policy.setStatus(PolicyStatus.PENDING_VALIDATION);
        policy.setLastUpdateDateTime(Instant.now());
        policyRepository.save(policy);

        // Send Email
        try {
            emailService.sendPolicyBookedEmail(policy);
        } catch (Exception e) {
            logger.error(String.format("Unable to send email for booking policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-booked-sms.txt"), Charset.forName("UTF-8"));
            String fullName = policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName();
            SMSResponse m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()).replace("%FULL_NAME%", fullName));
            if (!m.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
                logger.error(String.format("SMS for policy booking could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (Exception e) {
            logger.error(String.format("Unable to send policy booking SMS message on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/policy-booked-notification.txt"), Charset.forName("UTF-8"));
            String sendMsg = pushContent.replace("%POLICY_ID%", policy.getPolicyId());
            sendMsg = sendMsg.replace("%FULL_NAME%", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName());
            lineService.sendPushNotification(sendMsg, policy.getInsureds().get(0).getPerson().getLineId());
        } catch (Exception e) {
            logger.error(String.format("Unable to send push notification for policy booking on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send Application Form to TMC
        DocumentDownload applicationFormDocument = documentService.findDocumentDownload(applicationFormPdf.get().getId());
        try {
            tmcClient.sendPDFToTMC(policy, applicationFormDocument.getContent(), APPLICATION_FORM);
        } catch (Exception e) {
            logger.error("Unable to send application Form to TMC on policy [" + policy.getPolicyId() + "]: " + e.getMessage(), e);
        }

        // Send DA Form to TMC (DA form may not exist)
        if (daFormPdf.isPresent()) {
            DocumentDownload daFormDocument = documentService.findDocumentDownload(daFormPdf.get().getId());
            try {
                tmcClient.sendPDFToTMC(policy, daFormDocument.getContent(), DA_FORM);
            } catch (Exception e) {
                logger.error("Unable to send DA Form to TMC on policy [" + policy.getPolicyId() + "]: " + e.getMessage(), e);
            }
        }
    }

    public Policy updatePolicyAfterPolicyHasBeenValidated(Policy policy, String agentCode, String agentName, String token) {
        if (!PolicyStatus.PENDING_VALIDATION.equals(policy.getStatus())) {
            throw new ElifeException("Can't validate policy [" + policy.getPolicyId() + "], it is not pending for validation, it's " + policy.getStatus());
        }

        // This has to be set asap since it is used to generate document
        policy.setValidationAgentCode(agentCode);
        policy.setValidationAgentName(agentName);

        // Generate documents
        try {
            documentService.generateValidatedPolicyDocuments(policy, token);
        } catch (Exception e) {
            throw new ElifeException("Can't generate documents for the policy [" + policy.getPolicyId() + "]: " + e.getMessage(), e);
        }

        // Should block if eReceipt is not generated
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (!documentPdf.isPresent()) {
            throw new ElifeException("Can't find signed eReceipt for the policy [" + policy.getPolicyId() + "]");
        }

        // Should block if validated Application FormProductIFineTest is not generated
        Optional<Document> applicationFormValidatedPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM_VALIDATED)).findFirst();
        if (!applicationFormValidatedPdf.isPresent()) {
            throw new ElifeException("Can't find validated application form for the policy [" + policy.getPolicyId() + "]");
        }

        policy.setStatus(VALIDATED);
        Instant now = Instant.now();
        policy.setValidationDateTime(now);
        policy.setLastUpdateDateTime(now);
        policy = policyRepository.save(policy);
        logger.info(String.format("Policy [%1$s] has been updated as Validated.", policy.getPolicyId()));

        // Send Email
        DocumentDownload documentDownload = documentService.findDocumentDownload(documentPdf.get().getId());
        try {
            emailService.sendEreceiptEmail(policy, Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policy.getPolicyId() + ".pdf"));
        } catch (Exception e) {
            logger.error(String.format("Unable to send email for validation of policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-purchased-sms.txt"), Charset.forName("UTF-8"));
            SMSResponse m = smsApiService.sendConfirmationMessage(policy, smsContent);
            if (!m.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
                logger.error(String.format("SMS for policy validation could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (Exception e) {
            logger.error(String.format("Unable to send policy validation SMS message on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/policy-purchased-notification.txt"), Charset.forName("UTF-8"));
            lineService.sendPushNotification(pushContent, policy.getInsureds().get(0).getPerson().getLineId());
        } catch (Exception e) {
            logger.error(String.format("Unable to send push notification for policy validation on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Sign eReceipt and send it to TMC
        try {
            tmcClient.sendPDFToTMC(policy, documentDownload.getContent(), ERECEIPT_PDF);
        } catch (Exception e) {
            logger.error("Unable to send eReceipt to TMC on policy [" + policy.getPolicyId() + "]:" + e.getMessage(), e);
        }

        // Send Validated Application Form to TMC
        DocumentDownload applicationFormValidatedDocument = documentService.findDocumentDownload(applicationFormValidatedPdf.get().getId());
        try {
            tmcClient.sendPDFToTMC(policy, applicationFormValidatedDocument.getContent(), APPLICATION_FORM);
        } catch (Exception e) {
            logger.error("Unable to send validated application Form to TMC on policy [" + policy.getPolicyId() + "]: " + e.getMessage(), e);
        }
        return policy;
    }

    public Pair<byte[], String> findEreceiptAttachmentByDocumentId(String policyId, String documentId) {
        DocumentDownload documentDownload = documentService.findDocumentDownload(documentId);
        return Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policyId + ".pdf");
    }

    //TODO change logic: if send email fail, it still continue with SMS & line push notification, then after that return error for email.
    public void sendNotificationsWhenUserNotRespondingToCalls(Policy policy) throws IOException, MessagingException {
        // Send Email
        emailService.sendUserNotRespondingEmail(policy);

        // Send SMS
        String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/user-not-responging-sms.txt"), Charset.forName("UTF-8"));
        SMSResponse m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()));
        if (!m.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
            throw new IOException(String.format("SMS when user not responding could not be sent on policy [%1$s].", policy.getPolicyId()));
        }

        // Send push notification
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/user-not-responging-notification.txt"), Charset.forName("UTF-8"));
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), mainInsured.getPerson().getLineId());
    }

    //TODO change logic: if send email fail, it still continue with line push notification, then after that return error for email.
    public void sendNotificationsWhenPhoneNumberIsWrong(Policy policy) throws IOException, MessagingException {
        // Send Email
        emailService.sendPhoneNumberIsWrongEmail(policy);

        // Send push notification
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/pushnotification-content/phone-number-wrong-notification.txt"), Charset.forName("UTF-8"));
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        lineService.sendPushNotification(pushContent.replace("%POLICY_ID%", policy.getPolicyId()), mainInsured.getPerson().getLineId());
    }

    private void updatePayment(Payment payment, Double value, String currencyCode, ChannelType channelType, String errorCode, String errorMessage, String registrationKey, String creditCardName, String paymentMethod) {
        SuccessErrorStatus status;
        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            status = SuccessErrorStatus.ERROR;
            errorMessage = "Currencies are different";
            errorCode = LineService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
        } else if (!isEmpty(errorCode) && !errorCode.equals("0000")) {
            status = SuccessErrorStatus.ERROR;
        } else {
            status = SuccessErrorStatus.SUCCESS;
        }

        // registration key might have to be updated
        if (!isBlank(registrationKey) && !registrationKey.equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey);
        }
        LocalDate nowInThai = DateTimeUtil.nowLocalDateInThaiZoneId();
        LocalDateTime nowDateTimeInThai = DateTimeUtil.nowLocalDateTimeInThaiZoneId();

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount(value, currencyCode));
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setDate(nowInThai);
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setRejectionErrorCode(errorCode);
        paymentInformation.setRejectionErrorMessage(errorMessage);
        paymentInformation.setStatus(status);
        payment.getPaymentInformations().add(paymentInformation);

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SuccessErrorStatus.SUCCESS))
                .mapToDouble(tmp -> tmp.getAmount().getValue())
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(INCOMPLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(COMPLETED);
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(OVERPAID);
        }
        payment.setEffectiveDate(nowDateTimeInThai);
        paymentRepository.save(payment);
        logger.info("Payment [" + payment.getPaymentId() + "] has been updated");
    }
}
