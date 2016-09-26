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
        return "ORDER_" + RandomStringUtils.randomNumeric(10);
    }

    public static String generateRegKeyId() {
        return "REGKEY_" + RandomStringUtils.randomAlphanumeric(15);
    }

    public static String generateTransactionId() {
        return "TRANS_" + RandomStringUtils.randomAlphanumeric(20);
    }

    public static String generateAccessToken() {
        return "ACCESSTOKEN_" + RandomStringUtils.randomAlphanumeric(25);
    }
}
