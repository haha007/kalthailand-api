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
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBViewRepository;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;

import javax.inject.Inject;
import java.util.Collections;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentFailEmailService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentFailEmailService.class);
    public static final String RESPONSE_CODE_EMAIL_SENT_SUCCESS = "0000";

    private final ElifeEmailService emailService;
    private final MessageSource messageSource;
    private final PaymentRetryLinkService paymentRetryLinkService;
    private final CDBViewRepository cdbViewRepository;

    @Inject
    public PaymentFailEmailService(ElifeEmailService emailService,
                                   MessageSource messageSource,
                                   PaymentRetryLinkService paymentRetryLinkService,
                                   CDBViewRepository cdbViewRepository) {
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.paymentRetryLinkService = paymentRetryLinkService;
        this.cdbViewRepository = cdbViewRepository;
    }

    public void sendEmail(Policy policy, Payment payment) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        String insuredEmail = mainInsured.getPerson().getEmail();
        if (StringUtils.isBlank(insuredEmail)) {
            throw new EmailException("Insured customer doesn't have email, so cannot send email to customer.");
        }

        String emailSubject = messageSource.getMessage("email.payment.fail.title", null, LocaleUtil.THAI_LOCALE);
        String emailContent = fillEmailContent(policy, payment);
        emailService.sendEmail(insuredEmail, emailSubject, emailContent
//                , //EmailElifeUtil.initImagePairs("logo")
                , Collections.emptyList());
        LOGGER.debug("Sent informed email to customer, policyId: " + policy.getPolicyId());
    }

    private String fillEmailContent(Policy policy, Payment payment) {
        String emailContent = IOUtil.loadTextFileInClassPath("/email-content/email-payment-fail.html");
        String paymentLink = "";
        String productDisplayName = "";
        String customerName = "";
        String cdbDueDateString = "";
        String paymentAmount = "";
        try {
            paymentLink = paymentRetryLinkService.createPaymentLink(policy.getPolicyId(), payment);

            String productId = policy.getCommonData().getProductId();
            ProductType productType = ProductUtils.validateExistProductTypeByLogicName(productId);
            productDisplayName = productType.getDisplayName();
            Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
            Person insuredPerson = mainInsured.getPerson();
            customerName = PersonUtil.getFullName(insuredPerson);
            if (payment != null) {
                cdbDueDateString = cdbViewRepository.getPaymentDueDate(policy.getPolicyId());
                Amount amount = payment.getAmount();
                paymentAmount = "" + amount.getValue();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            //get due-date of policy from CDB first, In case could not get due-date, we will get due-date from MongoDB
            String thaiDueDate = DateTimeUtil.formatThaiDate(StringUtils.isNotEmpty(cdbDueDateString)
                    ? DateTimeUtil.toLocalDate(cdbDueDateString, DateTimeUtil.PATTERN_CDB_DUEDATE)
                    : payment.getDueDate().toLocalDate());

            emailContent = emailContent.replaceAll("%PRODUCT_NAME%", productDisplayName)
                    .replaceAll("%POLICY_NUMBER%", policy.getPolicyId())
                    .replaceAll("%CUSTOMER_NAME%", customerName)
                    .replaceAll("%PAYMENT_DUE_DATE%", thaiDueDate)
                    .replaceAll("%PAYMENT_AMOUNT%", "" + paymentAmount)
                    .replaceAll("%PAYMENT_LINK%", paymentLink);
        }
        return emailContent;
    }

}
