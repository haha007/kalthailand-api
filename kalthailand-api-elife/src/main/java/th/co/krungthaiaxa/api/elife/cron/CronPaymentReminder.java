package th.co.krungthaiaxa.api.elife.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.line.v2.service.LineService;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.urlshortener.google.UrlShortenerService;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * @author tuong.le on 11/29/17.
 */
@Service
public class CronPaymentReminder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CronPaymentReminder.class);
    private static final String EMAIL_REMINDER_SUBJECT = "/email-content/email-remind-payment-subject.txt";
    private static final String EMAIL_REMINDER_CONTENT = "/email-content/email-remind-payment.html";
    private static final String LINE_NOTIFICATION_REMINDER_CONTENT = "/line-notification/line-notification-remind-payment.txt";

    private final LineService lineService;
    private final ElifeEmailService elifeEmailService;
    private final PolicyService policyService;
    private final UrlShortenerService urlShortenerService;
    @Value("${kal.elife.th.url}")
    private String elifeUrl;

    @Inject
    private MessageSource messageSource;

    @Inject
    public CronPaymentReminder(LineService lineService,
                               ElifeEmailService elifeEmailService,
                               PolicyService policyService,
                               UrlShortenerService urlShortenerService) {
        this.lineService = lineService;
        this.elifeEmailService = elifeEmailService;
        this.policyService = policyService;
        this.urlShortenerService = urlShortenerService;
    }


    @Scheduled(cron = "0 0 12 * * *")
    // @Scheduled(cron = "0 15 * * * *")
    public void cronPaymentReminder() {
        LOGGER.info("Start Sending Payment Reminder to customer ------->");
        final LocalDate policiesDate = LocalDate.now().minusDays(3);
        // final LocalDate policiesDate = LocalDate.now(); // TOday
        LOGGER.info("Retrieve all pending payment policies on {}", policiesDate);
        final List<Policy> pendingPaymentPolicies = policyService.findAllPolicyByStatusOnDate(PolicyStatus.PENDING_PAYMENT, policiesDate);
        LOGGER.info("Processing {} policies", pendingPaymentPolicies.size());
        pendingPaymentPolicies.forEach(policy -> {
            final Locale thLocale = new Locale("th", "");
            
            // get shorten url
            final String remindLink = elifeUrl + "remind-payment/" + policy.getPolicyId();
            //final String shortenUrl = urlShortenerService.getShortUrl(remindLink);
            final String shortenUrl = remindLink;

            final Person person = ProductUtils.validateExistFirstInsured(policy).getPerson();
            final String fullName = person.getGivenName() + " " + person.getSurName();
            final String productName = policy.getCommonData().getProductId();
            final String userEmail = person.getEmail();
            final String sumInsure = ProductUtils.getSumInsureAsString(policy);

            //  Email:
            try {
                String emailSubject = IOUtil.loadTextFileInClassPath(EMAIL_REMINDER_SUBJECT);
                emailSubject = emailSubject.replace("%PRODUCT%", policy.getCommonData().getProductId());

                String emailRemindContent = IOUtil.loadTextFileInClassPath(EMAIL_REMINDER_CONTENT);
                emailRemindContent = emailRemindContent
                        .replace("%FULLNAME%", fullName)
                        .replace("%PRODUCT%", productName)
                        .replace("%SUM_INSURE%", sumInsure)
                        .replace("%PREMIUM%", (new DecimalFormat("#,##0.00"))
                                .format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                        .replace("%PAYMENT_MODE%",
                                messageSource.getMessage("payment.mode." + policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale))
                        .replace("%REMIND_LINK%", shortenUrl);
                elifeEmailService.sendEmail(userEmail, emailSubject, emailRemindContent);
                LOGGER.info("Sent Payment Reminder to {}", fullName);
            } catch (Exception ex) {
                LOGGER.error("Could not send pending payment reminder to unknown email for policy Id {}: ", policy.getPolicyId(), ex);
            }

            //  LINE push:
            final String lineUserId = person.getLineUserId();
            final String notificationMessage = IOUtil.loadTextFileInClassPath(LINE_NOTIFICATION_REMINDER_CONTENT);
            final String msgContent = notificationMessage
                    .replace("%FULLNAME%", fullName)
                    .replace("%PRODUCT%", productName)
                    .replace("%REMIND_LINK%", shortenUrl);
            if (lineService.pushTextMessage(lineUserId, msgContent)) {
                LOGGER.info("Pushed Line Message to {}", fullName);
            } else {
                LOGGER.error("Could not pushed Line Message to {}", fullName);
            }

            //Insert Record to db to monitor

            LOGGER.info("End Sending Payment Reminder to customer <-------");

        });
    }
}
