package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.LocaleUtil;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptNumber;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptService;
import th.co.krungthaiaxa.api.elife.exception.LinePaymentException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.utils.EmailElifeUtil;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author khoi.tran on 11/1/16.
 */
@Service
public class PaymentRetryService {
    public final static Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    /**
     * For retry payment, it's always Renewal payment, so the NewBusiness is alwasy false.
     */
    private static final boolean NEW_BUSINESS = false;
    /**
     * Need getter-setter for mocking.
     */
    private LineService lineService;
    private final EreceiptService ereceiptService;
    private final PolicyService policyService;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final MessageSource messageSource;
    private final PaymentRepository paymentRepository;
    private final GeneralSettingService generalSettingService;

    @Inject
    public PaymentRetryService(GeneralSettingService generalSettingService, PaymentRepository paymentRepository, PolicyService policyService, LineService lineService, EmailService emailService, EreceiptService ereceiptService,
            MessageSource messageSource, PaymentService paymentService) {
        this.paymentService = paymentService;
        this.generalSettingService = generalSettingService;
        this.paymentRepository = paymentRepository;
        this.policyService = policyService;
        this.lineService = lineService;
        this.emailService = emailService;
        this.ereceiptService = ereceiptService;
        this.messageSource = messageSource;
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
        Payment oldPayment = paymentService.validateNotExistNewerPayment(oldPaymentId);
        boolean newBusiness = NEW_BUSINESS;

        Payment retryPayment = new Payment();
        retryPayment.setPolicyId(policyId);
        retryPayment.setRegistrationKey(regKey);
        retryPayment.setDueDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        retryPayment.setAmount(oldPayment.getAmount());
        retryPayment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        retryPayment.setTransactionId(transactionId);
        retryPayment.setOrderId(orderId);
        retryPayment.setRetried(true);

        // If no transaction id, then in error, nothing else should be done since we don't have a status (error / success)
        if (StringUtils.isBlank(transactionId)) {
            throw new BadArgumentException("Transaction doesn't exist");
        }

        LinePayResponse linePayResponse = null;
        try {
            LOGGER.debug("Will try to confirm payment with transation ID [" + transactionId + "] on the policy with ID [" + policyId + "]");
            linePayResponse = lineService.capturePayment(transactionId, retryPayment.getAmount().getValue(), retryPayment.getAmount().getCurrencyCode());
        } catch (Exception e) {
            throw new LinePaymentException("Unable to confirm the payment in the policy with ID [" + policyId + "]", e);
        } finally {
            if (linePayResponse != null && LineService.RESPONSE_CODE_SUCCESS.equals(linePayResponse.getReturnCode())) {
                EreceiptNumber ereceiptNumber = ereceiptService.generateEreceiptFullNumber(newBusiness);
                retryPayment.setReceiptNumber(ereceiptNumber);
                retryPayment.setNewBusiness(newBusiness);
                retryPayment.setReceiptNumberOldPattern(false);
                retryPayment.setStatus(PaymentStatus.COMPLETED);
            } else {
                retryPayment.setStatus(PaymentStatus.INCOMPLETE);
                LOGGER.warn("The retry payment also not successed yet. policyId: {}, oldPaymentId: {}, orderId: {}, transactionId: {}", policyId, oldPaymentId, orderId, transactionId);
                //Don't need to resend another fail email to user. When backend return error, FE will show error page to customer.
            }
            //TODO payment with response
            //TODO following line is redundant.
//            retryPayment = paymentService.updatePayment(retryPayment, orderId, transactionId, StringUtils.isBlank(regKey) ? "" : regKey); //Unnessary
            retryPayment = paymentService.updateByLinePayResponse(retryPayment, linePayResponse);//Old code: missing some information, but still correct
//            paymentService.updatePaymentAfterLinePay(retryPayment, retryPayment.getAmount().getValue(), retryPayment.getAmount().getCurrencyCode(), ChannelType.LINE, linePayResponse);//New method with correct information.
            oldPayment.setRetryPaymentId(retryPayment.getPaymentId());
            paymentRepository.save(oldPayment);
        }
        Policy policy = policyService.validateExistPolicy(retryPayment.getPolicyId());
        sendRetryPaymentSuccessEmailToInsuredPersonAndMarketingTeam(policy, retryPayment, accessToken);
        sendRetryPaymentSuccessLineNotificationToInsuredPerson(policy);
        return retryPayment;
    }

    /**
     * This code is used only for retry, not for the first payment.
     *
     * @param payment
     * @param accessToken
     */
    private void sendRetryPaymentSuccessEmailToInsuredPersonAndMarketingTeam(Policy policy, Payment payment, String accessToken) {
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
        emailService.sendEmails(toEmails, emailSubject, emailContent, EmailElifeUtil.initImagePairs("logo"), Arrays.asList(ereceiptAttachment));
    }

    private void sendRetryPaymentSuccessLineNotificationToInsuredPerson(Policy policy) {
        String mid = ProductUtils.getMid(policy);
        String notificationMessage = IOUtil.loadTextFileInClassPath("/line-notification/line-notification-payment-retry-success.txt");
        notificationMessage = notificationMessage.replaceAll("%POLICY_NUMBER%", policy.getPolicyId());
        lineService.sendPushNotification(notificationMessage, mid);
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
