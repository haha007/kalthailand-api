package th.co.krungthaiaxa.api.elife.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.Payment;

/**
 * @author khoi.tran on 9/26/16.
 */
@Component
public class PaymentFactory {
    public static void generateRandomValuesForPayment(Payment payment) {
        payment.setTransactionId(generateTransactionId());
        payment.setOrderId(generateOrderId());
        payment.setRegistrationKey(generateRegKeyId());
    }

    public static String generateOrderId() {
        return "MOCKORDER_" + System.currentTimeMillis();
    }

    public static String generateRegKeyId() {
        return "MOCKREGKEY_" + RandomStringUtils.randomAlphanumeric(15);
    }

    public static String generateTransactionId() {
        return "MOCKTRANS_" + System.currentTimeMillis();
    }

}
