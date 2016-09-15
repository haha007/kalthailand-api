package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.EmailException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentInformService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentInformService.class);

    private final EmailService emailService;

    @Value("${fe.host}/${fe.payment.contextpath}")
    private String paymentPath;

    @Inject
    public PaymentInformService(EmailService emailService) {this.emailService = emailService;}

    public void sendPaymentFailEmail(Policy policy, Payment payment) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        String insuredEmail = mainInsured.getPerson().getEmail();
        if (StringUtils.isBlank(insuredEmail)) {
            throw new EmailException("Insured customer doesn't have email, so cannot send email to customer.");
        }

        String emailSubject = "[Elife] Payment for policy is not success: ";
        String emailContent = fillEmailContent(policy, payment);
        emailService.sendEmail(insuredEmail, emailSubject, emailContent, Collections.EMPTY_LIST);
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
            customerName = createFullName(insuredPerson.getGivenName(), insuredPerson.getMiddleName(), insuredPerson.getSurName());
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

    private String createFullName(String givenName, String middleName, String surName) {
        String customerName = givenName + (StringUtils.isBlank(middleName) ? "" : " " + middleName) + (StringUtils.isBlank(surName) ? "" : " " + middleName);
        return customerName;
    }

    private String createPaymentLink(String policyNumber, Payment payment) {
        if (payment != null) {
            return String.format("%s?policyNumber=%s&paymentId=%s", paymentPath, policyNumber, payment.getPaymentId());
        } else {
            //We terrible sorry but cannot help user to payment again by himself: cannot generate payment link without payment and policyNumber. We need to contact and handle this case manually.
            return "";
        }
    }
}
