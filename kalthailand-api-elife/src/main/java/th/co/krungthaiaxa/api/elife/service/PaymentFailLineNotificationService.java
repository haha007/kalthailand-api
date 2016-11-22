package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.exception.LinePaymentException;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.PersonUtil;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentFailLineNotificationService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentFailLineNotificationService.class);
    private static final String NOTIFICATION_PATH = "/line-notification/line-notification-payment-fail.txt";
    public static final String RESPONSE_CODE_SENT_SUCCESS = "0000";
    private final LineService lineService;
    private final PaymentRetryLinkService paymentRetryLinkService;

    @Autowired
    public PaymentFailLineNotificationService(LineService lineService, PaymentRetryLinkService paymentRetryLinkService) {
        this.lineService = lineService;
        this.paymentRetryLinkService = paymentRetryLinkService;
    }

    public void sendNotification(Policy policy, Payment payment) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
        String mid = mainInsured.getPerson().getLineId();
        if (StringUtils.isBlank(mid)) {
            throw new LinePaymentException("Insured customer doesn't have lineId, so cannot send line notification to customer.");
        }
        String pushContent = getNotificationContent(policy, payment);
        lineService.sendPushNotification(pushContent, mid);
    }

    public void sendNotificationIgnoreError(Policy policy, Payment payment) {
        try {
            sendNotification(policy, payment);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to send LINE push notification for retry payment: %s\n policyId: %s, paymentId: %s", e.getMessage(), policy.getPolicyId(), payment.getPaymentId()), e);
        }
    }

    private String getNotificationContent(Policy policy, Payment payment) {
        String lineNotificationContent = IOUtil.loadTextFileInClassPath(NOTIFICATION_PATH);
        Insured mainInsured = ProductUtils.validateMainInsured(policy);
        String customerName = PersonUtil.getFullName(mainInsured.getPerson());
        String paymentLink = paymentRetryLinkService.createPaymentLink(policy.getPolicyId(), payment);
        lineNotificationContent = lineNotificationContent
                .replaceAll("%INSURED_FULL_NAME%", customerName)
                .replaceAll("%RETRY_PAYMENT_URL%", paymentLink);
        return lineNotificationContent;
    }

}
