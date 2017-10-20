package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.client.CDBClient;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyNotFoundException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.line.v2.service.LineService;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.MocabStatus;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.PersonInfo;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductServiceFactory;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyCriteriaRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.thirdParty.mocab.MocabClient;
import th.co.krungthaiaxa.api.elife.thirdParty.mocab.MocabResponse;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM_VALIDATED;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.DA_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.NOT_PROCESSED;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.VALIDATED;

//TODO need to be refactored.
@Service
public class PolicyService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PolicyService.class);

    private final TMCClient tmcClient;
    private final PaymentRepository paymentRepository;
    private final PolicyCriteriaRepository policyCriteriaRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNumberRepository policyNumberRepository;
    private final QuoteRepository quoteRepository;
    private final EmailService emailService;
    private final LineService lineService;
    private final DocumentService documentService;
    private final PolicyDocumentService policyDocumentService;
    private final SMSApiService smsApiService;
    private final ProductServiceFactory productServiceFactory;
    private final CDBClient cdbClient;
    private final BeanValidator beanValidator;
    private final MocabClient mocabClient;

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
                         LineService lineService,
                         DocumentService documentService,
                         PolicyDocumentService policyDocumentService, SMSApiService smsApiService,
                         ProductServiceFactory productServiceFactory, CDBClient cdbClient, BeanValidator beanValidator, MocabClient mocabClient) {
        this.tmcClient = tmcClient;
        this.policyDocumentService = policyDocumentService;
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
        this.beanValidator = beanValidator;
        this.mocabClient = mocabClient;
    }

    public Page<Policy> findAll(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate startDate,
                                LocalDate endDate, PeriodicityCode periodicityCode, Integer atpModeId, Integer currentPage, Integer pageSize) {
        return policyCriteriaRepository.findPolicies(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, periodicityCode, atpModeId, new PageRequest(currentPage, pageSize, new Sort(Sort.Direction.DESC, "policyId")));
    }

    public List<Policy> findAll(String policyId, ProductType productType, PolicyStatus status, Boolean nonEmptyAgentCode, LocalDate startDate,
                                LocalDate endDate, PeriodicityCode periodicityCode, Integer atpModeId) {
        return policyCriteriaRepository.findPolicies(policyId, productType, status, nonEmptyAgentCode, startDate, endDate, periodicityCode, atpModeId);
    }

    public Optional<Policy> findPolicyByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        return policy != null ? Optional.of(policy) : Optional.empty();
    }

    public Policy findPolicyWithFullDetailsByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        if (policy != null) {
            Sort sort = new Sort(Sort.Direction.DESC, "dueDate", "effectiveDate");
            List<Payment> payments = paymentRepository.findByPolicyId(policyNumber, sort);
            policy.setPayments(payments);
        }
        return policy;
    }

    public Policy validateExistPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyId(policyNumber);
        if (policy == null) {
            throw new PolicyNotFoundException("Not found policy " + policyNumber);
        }
        return policy;
    }

    /**
     * It must be synchronized to avoid many new policies with the same number.
     *
     * @return
     */
    private synchronized PolicyNumber validateExistNextAvailablePoliyNumber() {
        Sort sort = new Sort(Sort.Direction.ASC, "policyId");
        Pageable pageable = new PageRequest(0, 1, sort);
        Page<PolicyNumber> policyNumbers = policyNumberRepository.findByPolicyNull(pageable);
        if (!policyNumbers.iterator().hasNext()) {
            throw PolicyValidationException.noPolicyNumberAvailable;
        }
        return policyNumbers.iterator().next();
    }

    /**
     * @param quote the quote must be calculated ({@link QuoteService#createQuote(String, ChannelType, ProductQuotation)}
     * @return After creating policy, its status will be {@link PolicyStatus#PENDING_PAYMENT}.
     */
    public Policy createPolicy(Quote quote) {
        notNull(quote, PolicyValidationException.emptyQuote);
        notNull(quote.getId(), PolicyValidationException.noneExistingQuote);
        notNull(quoteRepository.findOne(quote.getId()), PolicyValidationException.noneExistingQuote);

        PolicyNumber policyNumber = validateExistNextAvailablePoliyNumber();

        Policy policy = policyRepository.findByQuoteId(quote.getId());
        if (policy == null) {
            LOGGER.info("Creating Policy from quote [" + quote.getQuoteId() + "]");
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
            LOGGER.info("Policy has been created with id [" + policy.getPolicyId() + "] from quote [" + quote.getQuoteId() + "]");
        }

        return policy;
    }

    public void updateRegKeyForAllNotProcessedPayments(Policy policy, String newRegistrationKey) {
        Instant start = LogUtil.logStarting("updateRegKeyForAllNotProcessedPayments [start]. policyId: " + policy.getPolicyId());
        if (!isBlank(newRegistrationKey)) {
            //TODO Improve performance: use Mongo query to update
            // Save the registration key in all other payments
            List<Payment> payments = paymentRepository.findByPolicyId(policy.getPolicyId());
            payments.stream().filter(paymentPredicate -> paymentPredicate.getStatus().equals(NOT_PROCESSED)).forEach(payment -> {
                if (!newRegistrationKey.equals(payment.getRegistrationKey())) {
                    payment.setRegistrationKey(newRegistrationKey);
                }
            });
        }
        LogUtil.logFinishing(start, "updateRegKeyForAllNotProcessedPayments [finish]. policyId: " + policy.getPolicyId());
    }

    /**
     * This method is called only after the first payment was set orderId, transactionId and regKey.
     * TODO should refactor: before change the status of policy, the first payment must always has orderId, transactionId and regKey. So we should put the updatePayment inside this method.
     * Flow:
     * 1) A quote is created
     * 2) A policy is created from quote
     * 3) The first payment is paid by customer via FE & LINE pay (normal process - not preApproval process).
     * 4) Then we will have orderId, transactionId, and regKey (from LINE service). Those information will be updated into payment and the policy status is PENDING_PAYMENT (this method)
     * 5) Call method {@link #updatePolicyStatusToValidated(Policy, String, String, String)} so that the payment is captured to LINE service and the status of the policy become VALIDATED.
     *
     * @param policy
     */
    public Policy updatePolicyStatusToPendingValidation(Policy policy) {
        // Generate documents
        try {
            policyDocumentService.generateDocumentsForPendingValidationPolicy(policy);
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
        if (!daFormPdf.isPresent() && ProductUtils.isAtpModeEnable(policy)) {
            throw new ElifeException("Can't find DA form for the policy [" + policy.getPolicyId() + "] while it is mandatory for Monthly Policy");
        }

        // Update the policy
        /* Checking previous policy is no longer needed while validating policy, and prevents CBD issues(not avail 24/24) 
        Insured mainInsured = ProductUtils.getFirstInsured(policy);
        Optional<Registration> insuredRegistrationOptional = mainInsured.getPerson().getRegistrations()
                .stream()
                .filter(registration -> registration.getTypeName().equals(RegistrationTypeName.THAI_ID_NUMBER))
                .findFirst();
        String insuredDOB = mainInsured.getPerson().getBirthDate().format(ofPattern("yyyyMMdd"));
        if (insuredRegistrationOptional.isPresent()) {
            Optional<PreviousPolicy> previousPolicyOptional = cdbClient.getExistingAgentCode(insuredRegistrationOptional.get().getId(), insuredDOB);
            if (previousPolicyOptional.isPresent()) {
                PreviousPolicy previousPolicy = previousPolicyOptional.get();
                mainInsured.setPreviousPolicy(previousPolicy);
                mainInsured.addInsuredPreviousInformation(previousPolicy.getPolicyNumber());
                mainInsured.addInsuredPreviousInformation(previousPolicy.getAgentCode1());
                mainInsured.addInsuredPreviousInformation(previousPolicy.getAgentCode2());
            }
            mainInsured.setNotSearchedPreviousPolicy(false);
        }*/

        policy.setStatus(PolicyStatus.PENDING_VALIDATION);
        policy.setLastUpdateDateTime(Instant.now());
        policy = policyRepository.save(policy);

        // Send Email
        try {
            emailService.sendPolicyBookedEmail(policy);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send email for booking policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-booked-sms.txt"), Charset.forName("UTF-8"));
            String fullName = policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName();
            SMSResponse m = smsApiService.sendConfirmationMessage(policy, smsContent.replace("%POLICY_ID%", policy.getPolicyId()).replace("%FULL_NAME%", fullName));
            if (!m.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
                LOGGER.error(String.format("SMS for policy booking could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send policy booking SMS message on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/line-notification/policy-booked-notification.txt"), Charset.forName("UTF-8"));
            String sendMsg = pushContent.replace("%POLICY_ID%", policy.getPolicyId());
            sendMsg = sendMsg.replace("%FULL_NAME%", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName());
            lineService.pushTextMessage(policy.getInsureds().get(0).getPerson().getLineUserId(), sendMsg);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send LINE push notification for policy booking on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send Application Form to Mocab
        DocumentDownload applicationFormDocument =
                documentService.findDocumentDownload(applicationFormPdf.get().getId());
        sendPdfToMocab(policy, applicationFormDocument, APPLICATION_FORM);

        // Send DA Form to Mocab (DA form may not exist)
        if (daFormPdf.isPresent()) {
            DocumentDownload daFormDocument =
                    documentService.findDocumentDownload(daFormPdf.get().getId());
            sendPdfToMocab(policy, daFormDocument, DA_FORM);
        }
        return policy;
    }

    /**
     * Note: this method only update status of policy. It doesn't check other constraints.
     * If you want to upgrade status from PendingValidation to Validated, please use {@link PolicyValidatedProcessingService#updatePolicyStatusToValidated(PolicyValidatedProcessingService.PolicyValidationRequest)}
     *
     * @param policy
     * @param agentCode
     * @param agentName
     * @param token
     * @return
     */
    protected Policy updatePolicyStatusToValidated(Policy policy, String agentCode, String agentName, String token) {
        Instant start = LogUtil.logStarting("updatePolicyStatusToValidated [start]: policyId: " + policy.getPolicyId());
        if (!PolicyStatus.PENDING_VALIDATION.equals(policy.getStatus())) {
            throw new ElifeException("Can't validate policy [" + policy.getPolicyId() + "], it is not pending for validation, it's " + policy.getStatus());
        }

        // This has to be set asap since it is used to generate document
        policy.setValidationAgentCode(agentCode);
        policy.setValidationAgentName(agentName);

        // Generate documents
        try {
            policyDocumentService.generateDocumentsForValidatedPolicy(policy, token);
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
        LOGGER.info(String.format("Policy [%1$s] has been updated as Validated.", policy.getPolicyId()));

        // Send Email
        DocumentDownload documentDownload = documentService.findDocumentDownload(documentPdf.get().getId());
        try {
            emailService.sendEreceiptEmail(policy, Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policy.getPolicyId() + ".pdf"));
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send email for validation of policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send SMS
        try {
            String smsContent = IOUtils.toString(this.getClass().getResourceAsStream("/sms-content/policy-purchased-sms.txt"), Charset.forName("UTF-8"));
            SMSResponse m = smsApiService.sendConfirmationMessage(policy, smsContent);
            if (!m.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
                LOGGER.error(String.format("SMS for policy validation could not be sent on policy [%1$s].", policy.getPolicyId()));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send policy validation SMS message on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Send push notification
        try {
            String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/line-notification/policy-purchased-notification.txt"), Charset.forName("UTF-8"));
            lineService.pushTextMessage(policy.getInsureds().get(0).getPerson().getLineUserId(), pushContent);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send LINE push notification for policy validation on policy [%s]: %s", policy.getPolicyId(), e.getMessage()), e);
        }

        // Sign eReceipt and send it to Mocab
        sendPdfToMocab(policy, documentDownload, ERECEIPT_PDF);

        // Send Validated Application Form to Mocab
        final DocumentDownload applicationFormValidatedDocument =
                documentService.findDocumentDownload(applicationFormValidatedPdf.get().getId());
        sendPdfToMocab(policy, applicationFormValidatedDocument, APPLICATION_FORM_VALIDATED);

        // Update policy status in Mocab
        updatePolicyStatusMocab(policy, applicationFormValidatedPdf.get().getId());

        LogUtil.logFinishing(start, "updatePolicyStatusToValidated [finish]: policyId: " + policy.getPolicyId());
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
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/line-notification/user-not-responging-notification.txt"), Charset.forName("UTF-8"));
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        lineService.pushTextMessage(mainInsured.getPerson().getLineUserId(), pushContent.replace("%POLICY_ID%", policy.getPolicyId()));
    }

    //TODO change logic: if send email fail, it still continue with line push notification, then after that return error for email.
    public void sendNotificationsWhenPhoneNumberIsWrong(Policy policy) throws IOException, MessagingException {
        // Send Email
        emailService.sendPhoneNumberIsWrongEmail(policy);

        // Send push notification
        String pushContent = IOUtils.toString(this.getClass().getResourceAsStream("/line-notification/phone-number-wrong-notification.txt"), Charset.forName("UTF-8"));
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        lineService.pushTextMessage(mainInsured.getPerson().getLineUserId(), pushContent.replace("%POLICY_ID%", policy.getPolicyId()));
    }

    public Policy updateMainInsuredPerson(String policyId, PersonInfo mainInsuredPersonInfo) {
        beanValidator.validate(mainInsuredPersonInfo);
        Policy policy = validateExistPolicy(policyId);
        Insured mainInsured = ProductUtils.validateMainInsured(policy);
        Person person = mainInsured.getPerson();
        BeanUtils.copyProperties(mainInsuredPersonInfo, person);
        policy = policyRepository.save(policy);
        return policy;
    }

    private void sendPdfToMocab(final Policy policy,
                                final DocumentDownload documentDownload,
                                final DocumentType documentType) {
        try {
            final Optional<MocabResponse> mocabResponseOptional =
                    mocabClient.sendPdfToMocab(policy, documentDownload.getContent(), documentType);
            MocabStatus mocabStatus = new MocabStatus();
            mocabStatus.setPolicyNumber(policy.getPolicyId());
            if (mocabResponseOptional.isPresent()) {
                final MocabResponse mocabResponse = mocabResponseOptional.get();

                //The document status based on Mocab response code
                mocabStatus.setSuccess(mocabResponseOptional.get().isSuccess());
                mocabStatus.setMessageCode(mocabResponse.getMessageCode());
                mocabStatus.setMessageDetail(MocabResponse.mappingMessageDetail(mocabResponse.getMessageCode()));
                LOGGER.info("Sent {} to Mocab with response message code {} on policy {}",
                        documentType, mocabStatus.getMessageCode(), mocabStatus.getPolicyNumber());
                documentService.udpateDocumentStatus(documentDownload.getDocumentId(), mocabStatus);
                return;
            }
            documentService.udpateDocumentStatus(documentDownload.getDocumentId(), mocabStatus);

        } catch (Exception e) {
            LOGGER.error("Unable to send {} to Mocab on policy {}",
                    documentType.name(), policy.getPolicyId(), e);
        }

    }

    private void updatePolicyStatusMocab(final Policy policy, final String applicationFormValidatedId) {
        final Optional<MocabResponse> mocabResponseOptional =
                mocabClient.updatePolicyStatusMocab(policy, applicationFormValidatedId);
        final String policyId = policy.getPolicyId();
        final PolicyStatus policyStatus = policy.getStatus();
        if (!mocabResponseOptional.isPresent()) {
            LOGGER.error("Could not connect to MOCAB to update Status of Policy {}", policyId);
            return;
        }
        mocabResponseOptional.ifPresent(mocabResponse -> {
            if (mocabResponse.isSuccess()) {
                LOGGER.info("Status of Policy {} has been updated to {} in MOCAB", policyId, policyStatus);
            } else {
                LOGGER.error("Could not update Status of Policy {} in MOCAB with Error: {} - {}",
                        policyId, mocabResponse.getMessageCode(), MocabResponse.mappingMessageDetail(mocabResponse.getMessageCode()));
            }
        });
    }
}
