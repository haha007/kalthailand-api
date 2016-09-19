package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.LocaleUtil;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;
import th.co.krungthaiaxa.api.elife.exception.LinePaymentException;
import th.co.krungthaiaxa.api.elife.exception.PaymentNotFoundException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private final DocumentService documentService;
    private final MessageSource messageSource;

    @Inject
    public PaymentService(GeneralSettingService generalSettingService, PaymentRepository paymentRepository, PolicyService policyService, LineService lineService, EmailService emailService, DocumentService documentService, MessageSource messageSource) {
        this.generalSettingService = generalSettingService;
        this.paymentRepository = paymentRepository;
        this.policyService = policyService;
        this.lineService = lineService;
        this.emailService = emailService;
        this.documentService = documentService;
        this.messageSource = messageSource;
    }

    public Optional<Payment> findLastestPaymentByPolicyNumberAndRegKeyNotNull(String policyNumber) {
        return paymentRepository.findOneByRegKeyNotNullAndPolicyId(policyNumber, new Sort(Sort.Direction.DESC, "dueDate"));
    }

    public Payment findPaymentById(String paymentId) {
        return paymentRepository.findOne(paymentId);
    }

    /**
     * @param policyId
     * @param oldPaymentId
     * @param orderId
     * @param transactionId
     * @param regKey        this is the new registrationKey returned by lineservice, it should be different from current latest regKey in DB.
     * @param accessToken
     * @return
     */
    public Payment retryFailedPayment(String policyId, String oldPaymentId, String orderId, String transactionId, String regKey, String accessToken) {

        Payment oldPayment = validateExistPayment(oldPaymentId);
        if (StringUtils.isNotBlank(oldPayment.getRetryPaymentId())) {
            throw new BadArgumentException(String.format("The old payment Id %s was already retried by paymentId: %s", oldPaymentId, oldPayment.getRetryPaymentId()));
        }
        Payment payment = new Payment();
        payment.setPolicyId(policyId);
        payment.setRegistrationKey(regKey);
        payment.setDueDate(DateTimeUtil.nowLocalDateInThaiZoneId());
        payment.setAmount(oldPayment.getAmount());
        payment.setEffectiveDate(DateTimeUtil.nowLocalDateInThaiZoneId());
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
            payment = policyService.updatePayment(payment, orderId, transactionId, StringUtils.isBlank(regKey) ? "" : regKey);
            oldPayment.setRetryPaymentId(payment.getPaymentId());
            paymentRepository.save(oldPayment);
        }
        sendPaymentSuccessToMarketingTeam(payment, accessToken);
        return payment;
    }

    private Payment validateExistPayment(String paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("Not found payment with Id " + paymentId);
        }
        return payment;
    }

    private void sendPaymentSuccessToMarketingTeam(Payment payment, String accessToken) {
        Policy policy = policyService.validateExistPolicy(payment.getPolicyId());
        Document ereceiptPdfDocument = documentService.addEreceiptPdf(policy, payment, false, accessToken);

        String emailSubject = messageSource.getMessage("email.payment.retry.success.title", null, LocaleUtil.THAI_LOCALE);

        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        String emailContent = IOUtil.loadTextFileInClassPath("/email-content/email-retrypayment-success.html");
        emailContent.replaceAll("%PAYMENT_ID%", payment.getPaymentId());
        emailContent.replaceAll("%POLICY_NUMBER%", payment.getPolicyId());
        emailContent.replaceAll("%ORDER_ID%", payment.getOrderId());
        List<String> toEmails = generalSetting.getRetryPaymentSetting().getToSuccessEmails();
        String mainInsuredEmail = ProductUtils.validateExistMainInsured(policy).getPerson().getEmail();
        if (StringUtils.isNotBlank(mainInsuredEmail)) {
            toEmails.add(mainInsuredEmail);
        }
        Pair<byte[], String> ereceiptAttachment = policyService.findEreceiptAttachmentByDocumentId(policy.getPolicyId(), ereceiptPdfDocument.getId());
        emailService.sendEmailWithAttachments(toEmails, emailSubject, emailContent, Arrays.asList(ereceiptAttachment));
    }

    public LineService getLineService() {
        return lineService;
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
