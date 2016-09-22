package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.EmailException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.LocaleUtil;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentFailEmailService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentFailEmailService.class);
    public static final String RESPONSE_CODE_EMAIL_SENT_SUCCESS = "0000";

    private final EmailService emailService;
    private final GeneralSettingService generalSettingService;
    private final MessageSource messageSource;

    @Inject
    public PaymentFailEmailService(EmailService emailService, GeneralSettingService generalSettingService, MessageSource messageSource) {
        this.emailService = emailService;
        this.generalSettingService = generalSettingService;
        this.messageSource = messageSource;
    }

    public void sendPaymentFailEmail(Policy policy, Payment payment) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        String insuredEmail = mainInsured.getPerson().getEmail();
        if (StringUtils.isBlank(insuredEmail)) {
            throw new EmailException("Insured customer doesn't have email, so cannot send email to customer.");
        }

        String emailSubject = messageSource.getMessage("email.payment.fail.title", null, LocaleUtil.THAI_LOCALE);
        String emailContent = fillEmailContent(policy, payment);
        emailService.sendEmail(insuredEmail, emailSubject, emailContent, EmailUtil.initImagePairs("logo"), Collections.EMPTY_LIST);
//        emailService.sendEmail(insuredEmail, emailSubject, emailContent, Collections.EMPTY_LIST);
        LOGGER.debug("Sent informed email to customer, policyId: " + policy.getPolicyId());
    }

    private String fillEmailContent(Policy policy, Payment payment) {
        String emailContent = IOUtil.loadTextFileInClassPath("/email-content/email-payment-fail.html");
        String paymentLink = "";
        String productDisplayName = "";
        String customerName = "";
        String dueDateString = "";
        String paymentAmount = "";
        try {
            paymentLink = createPaymentLink(policy.getPolicyId(), payment);

            String productId = policy.getCommonData().getProductId();
            ProductType productType = ProductUtils.validateExistProductTypeByLogicName(productId);
            productDisplayName = productType.getDisplayName();
            Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
            Person insuredPerson = mainInsured.getPerson();
            customerName = PersonUtil.getFullName(insuredPerson);
            if (payment != null) {
                LocalDate dueDate = payment.getDueDate();
                dueDateString = DateTimeUtil.formatThaiDate(dueDate);
                Amount amount = payment.getAmount();
                paymentAmount = "" + amount.getValue();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            emailContent = emailContent.replaceAll("%PRODUCT_NAME%", productDisplayName)
                    .replaceAll("%POLICY_NUMBER%", policy.getPolicyId())
                    .replaceAll("%CUSTOMER_NAME%", customerName)
                    .replaceAll("%PAYMENT_DUE_DATE%", dueDateString)
                    .replaceAll("%PAYMENT_AMOUNT%", "" + paymentAmount)
                    .replaceAll("%PAYMENT_LINK%", paymentLink);
        }
        return emailContent;
    }

    private String createPaymentLink(String policyNumber, Payment payment) {
        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        String retryLink = generalSetting.getRetryPaymentSetting().getRetryLink();
        if (payment != null) {
            return String.format("%s?policyNumber=%s&paymentId=%s", retryLink, policyNumber, payment.getPaymentId());
        } else {
            //We terrible sorry but cannot help user to payment again by himself: cannot generate payment link without payment and policyNumber. We need to contact and handle this case manually.
            return "";
        }
    }
}
