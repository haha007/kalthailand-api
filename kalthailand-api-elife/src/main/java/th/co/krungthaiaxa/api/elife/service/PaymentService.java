package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.LocaleUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;
import th.co.krungthaiaxa.api.elife.exception.LinePaymentException;
import th.co.krungthaiaxa.api.elife.exception.PaymentHasNewerCompletedException;
import th.co.krungthaiaxa.api.elife.exception.PaymentNotFoundException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.api.elife.model.line.BaseLineResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.COMPLETED;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.INCOMPLETE;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentService {
    public final static Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final GeneralSettingService generalSettingService;
    private final PaymentRepository paymentRepository;
    private final PolicyService policyService;
    /**
     * Need getter-setter for mocking.
     */
    private LineService lineService;
    private final EmailService emailService;
    private final EreceiptService ereceiptService;
    private final MessageSource messageSource;

    @Inject
    public PaymentService(GeneralSettingService generalSettingService, PaymentRepository paymentRepository, PolicyService policyService, LineService lineService, EmailService emailService, EreceiptService ereceiptService,
            MessageSource messageSource) {
        this.generalSettingService = generalSettingService;
        this.paymentRepository = paymentRepository;
        this.policyService = policyService;
        this.lineService = lineService;
        this.emailService = emailService;
        this.ereceiptService = ereceiptService;
        this.messageSource = messageSource;
    }

    //TODO should move to {@link PaymentQueryService}
    public Payment findFirstPaymentHasTransactionId(String policyNumber) {
        Pageable pageable = new PageRequest(0, 1);
        List<Payment> payments = paymentRepository.findByPolicyIdAndTransactionIdNotNull(policyNumber, pageable);
        if (payments.isEmpty()) {
            return null;
        } else {
            return payments.get(0);
        }
    }

    public Optional<Payment> findLastestPaymentByPolicyNumberAndRegKeyNotNull(String policyNumber) {
        return paymentRepository.findOneByRegKeyNotNullAndPolicyId(policyNumber, new Sort(Sort.Direction.DESC, "dueDate"));
    }

    public Payment findPaymentById(String paymentId) {
        return paymentRepository.findOne(paymentId);
    }

    /**
     * Find closest newer payment which was competed.
     * For example:
     * payment 1: INCOMPLETED
     * payment 2: COMPLETED
     * payment 3: COMPLETED
     * <p>
     * result findNewerCompletedPayment(payment1) is payment2, not payment3
     *
     * @param oldPaymentId
     * @return
     */
    public PaymentNewerCompletedResult findNewerCompletedPaymentInSamePolicy(String oldPaymentId) {
        PaymentNewerCompletedResult result = new PaymentNewerCompletedResult();
        Payment oldPayment = validateExistPayment(oldPaymentId);
        result.setPayment(oldPayment);
        Payment newerCompletedPayment;
        if (oldPayment.getRetryPaymentId() != null) {
            Payment retryPayment = validateExistPayment(oldPayment.getRetryPaymentId());
            if (PaymentStatus.COMPLETED.equals(retryPayment.getStatus())) {
                result.setNewerCompletedPayment(retryPayment);
                return result;
            }
        }

        LocalDateTime oldEffectiveDate = oldPayment.getEffectiveDate();
        if (oldEffectiveDate != null) {
            newerCompletedPayment = paymentRepository.findOneByPolicyAndNewerEffectiveDate(oldPayment.getPolicyId(), oldEffectiveDate, PaymentStatus.COMPLETED);
        } else {
            LOGGER.warn("Something wrong: The old payment must be processed, so it must have effective date. But we cannot find effectiveDate of this paymentId: " + oldPayment.getPaymentId());
            newerCompletedPayment = paymentRepository.findOneByPolicyAndNewerId(oldPayment.getPaymentId(), oldPayment.getPaymentId(), PaymentStatus.COMPLETED);
        }
        result.setNewerCompletedPayment(newerCompletedPayment);
        return result;
    }

    public Payment validateNotExistNewerPayment(String paymentId) {
        PaymentNewerCompletedResult paymentNewerCompletedResult = findNewerCompletedPaymentInSamePolicy(paymentId);
        Payment oldPayment = paymentNewerCompletedResult.getPayment();
        Payment newerCompletedPayment = paymentNewerCompletedResult.getNewerCompletedPayment();
        if (newerCompletedPayment != null) {
            throw new PaymentHasNewerCompletedException(newerCompletedPayment, String.format("There's a newer payment which was completed: old payment Id: %s. Newer completed paymentId: %s", oldPayment.getPaymentId(), newerCompletedPayment.getPaymentId()));
        }
        return oldPayment;
    }

    /**
     * @param policyId
     * @param oldPaymentId
     * @param orderId
     * @param transactionId
     * @param regKey        this is the new registrationKey returned by lineservice, it should be different from current latest regKey in DB.
     * @param accessToken
     * @return the new retry payment
     */
    public Payment retryFailedPayment(String policyId, String oldPaymentId, String orderId, String transactionId, String regKey, String accessToken) {
        Payment oldPayment = validateNotExistNewerPayment(oldPaymentId);
        Payment payment = new Payment();
        payment.setPolicyId(policyId);
        payment.setRegistrationKey(regKey);
        payment.setDueDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        payment.setAmount(oldPayment.getAmount());
        payment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        payment.setTransactionId(transactionId);
        payment.setOrderId(orderId);

        // If no transaction id, then in error, nothing else should be done since we don't have a status (error / success)
        if (StringUtils.isBlank(transactionId)) {
            throw new BadArgumentException("Transaction doesn't exist");
        }

        LinePayResponse linePayResponse = null;
        try {
            LOGGER.debug("Will try to confirm payment with transation ID [" + transactionId + "] on the policy with ID [" + policyId + "]");
            linePayResponse = lineService.capturePayment(transactionId, payment.getAmount().getValue(), payment.getAmount().getCurrencyCode());
        } catch (Exception e) {
            throw new LinePaymentException("Unable to confirm the payment in the policy with ID [" + policyId + "]", e);
        } finally {
            if (linePayResponse != null && linePayResponse.getReturnCode().equals(LineService.RESPONSE_CODE_SUCCESS)) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.INCOMPLETE);
                LOGGER.warn("The retry payment also not successed yet. policyId: {}, oldPaymentId: {}, orderId: {}, transactionId: {}", policyId, oldPaymentId, orderId, transactionId);
                //Don't need to resend another fail email to user. When backend return error, FE will show error page to customer.
            }
            //TODO payment with response
            payment = policyService.updatePayment(payment, orderId, transactionId, StringUtils.isBlank(regKey) ? "" : regKey);
            payment = updateByLinePayResponse(payment, linePayResponse);
            oldPayment.setRetryPaymentId(payment.getPaymentId());
            paymentRepository.save(oldPayment);
        }
        sendRetryPaymentSuccessToMarketingTeam(payment, accessToken);
        return payment;
    }

    /**
     * @param payment
     * @param linePayResponse
     * @return
     */
    public Payment updateByLinePayResponse(Payment payment, BaseLineResponse linePayResponse) {

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setRejectionErrorCode(linePayResponse.getReturnCode());
        paymentInformation.setRejectionErrorMessage(linePayResponse.getReturnMessage());
        if (linePayResponse.getReturnCode().equals(LineService.RESPONSE_CODE_SUCCESS)) {
            String msg = "Success payment " + ObjectMapperUtil.toString(payment) + ". Response: " + ObjectMapperUtil.toString(linePayResponse);
            paymentInformation.setStatus(SuccessErrorStatus.SUCCESS);
            paymentInformation.setMethod(msg);
            payment.setStatus(COMPLETED);
        } else {
            payment.setStatus(INCOMPLETE);
        }
        payment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        payment.addPaymentInformation(paymentInformation);
        return paymentRepository.save(payment);
    }

    public Payment validateExistPayment(String paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("Not found payment with Id " + paymentId);
        }
        return payment;
    }

    /**
     * This code is used only for retry, not for the first payment.
     *
     * @param payment
     * @param accessToken
     */
    private void sendRetryPaymentSuccessToMarketingTeam(Payment payment, String accessToken) {
        Policy policy = policyService.validateExistPolicy(payment.getPolicyId());
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);

        Document ereceiptPdfDocument = ereceiptService.addEreceiptPdf(policy, payment, false, accessToken);

        String emailSubject = messageSource.getMessage("email.payment.retry.success.title", null, LocaleUtil.THAI_LOCALE);
        String emailContent = IOUtil.loadTextFileInClassPath("/email-content/email-retrypayment-success.html");

        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        String productDisplayName = ProductUtils.validateExistProductTypeByLogicName(policy.getCommonData().getProductId()).getDisplayName();
        emailContent = emailContent
                .replaceAll("%PAYMENT_ID%", payment.getPaymentId())
                .replaceAll("%POLICY_NUMBER%", payment.getPolicyId())
                .replaceAll("%CUSTOMER_NAME%", PersonUtil.getFullName(mainInsured.getPerson()))
                .replaceAll("%PAYMENT_DATE%", DateTimeUtil.formatThaiDateTime(payment.getEffectiveDate()))
                .replaceAll("%PAYMENT_AMOUNT%", String.valueOf(payment.getAmount().getValue()))
                .replaceAll("%PRODUCT_NAME%", productDisplayName);
        List<String> toEmails = generalSetting.getRetryPaymentSetting().getToSuccessEmails();
        String mainInsuredEmail = ProductUtils.validateExistMainInsured(policy).getPerson().getEmail();
        if (StringUtils.isNotBlank(mainInsuredEmail)) {
            toEmails.add(mainInsuredEmail);
        }
        Pair<byte[], String> ereceiptAttachment = policyService.findEreceiptAttachmentByDocumentId(policy.getPolicyId(), ereceiptPdfDocument.getId());
        emailService.sendEmails(toEmails, emailSubject, emailContent, EmailUtil.initImagePairs("logo"), Arrays.asList(ereceiptAttachment));
    }

    public LineService getLineService() {
        return lineService;
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
