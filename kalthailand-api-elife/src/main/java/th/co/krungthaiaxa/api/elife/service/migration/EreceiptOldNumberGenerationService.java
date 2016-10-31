package th.co.krungthaiaxa.api.elife.service.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 10/31/16.
 *         This class will generate the ereceipt numbers in the old ways (before 1.8.4 - 31-10-2016)
 */
@Service
public class EreceiptOldNumberGenerationService {
    public static final Logger LOGGER = LoggerFactory.getLogger(EreceiptOldNumberGenerationService.class);
    private final PaymentRepository paymentRepository;

    @Autowired
    public EreceiptOldNumberGenerationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * This method will run only one time.
     */
//    @PostConstruct
    public EreceiptOldNumberResult generateEreceiptNumbersByOldPatternForOldPayments() {
        List<Payment> paymentsInCludedRetryPayments = paymentRepository.findByReceiptPdfDocumentNotNullAndReceiptFullNumberBase36Null();
        LogUtil.logStarting("Payments included retryPayments: " + paymentsInCludedRetryPayments.size() + " \n" + toStringPaymentWithReceiptNumber(paymentsInCludedRetryPayments));

        Instant start = LogUtil.logStarting("Generate receipt numbers [start]");
        List<Payment> paymentsWithRetryPaymentIdNotNull = paymentRepository.findRetryPaymentIdByRetryPaymentIdNotNull();
        List<String> retryPaymentIds = paymentsWithRetryPaymentIdNotNull.stream().map(payment -> payment.getRetryPaymentId()).collect(Collectors.toList());

        //Handle for normal case
        List<Payment> payments = paymentRepository.findByReceiptPdfDocumentNotNullAndReceiptFullNumberBase36NullAndPaymentIdNotIn(retryPaymentIds);
        saveEreceiptNumbersByOldPattern(payments, true);
        start = LogUtil.logRuntime(start, "Generate receipt numbers for payments [finish]: " + payments.size() + "\n" + toStringPaymentWithReceiptNumber(payments));

        //Handle for retry payment

        List<Payment> retryPayments = paymentRepository.findByReceiptPdfDocumentNotNullAndReceiptFullNumberBase36NullAndPaymentIdIn(retryPaymentIds);
        saveEreceiptNumbersByOldPattern(retryPayments, false);
        LogUtil.logRuntime(start, "Generate receipt numbers for retry payments [finish]: " + retryPayments.size() + "\n" + toStringPaymentWithReceiptNumber(retryPayments));

        EreceiptOldNumberResult result = new EreceiptOldNumberResult();
        result.setNewBusinessPayments(payments);
        result.setRetryPayments(retryPayments);
        return result;
    }

    private String toStringPaymentWithReceiptNumber(List<Payment> payments) {
        return payments.stream().map(payment -> String.format("%s, receiptNumber: %s", payment.getPaymentId(), payment.getReceiptFullNumberBase36())).collect(Collectors.joining("\n"));
    }

    private void saveEreceiptNumbersByOldPattern(List<Payment> payments, boolean newBusiness) {
        for (Payment payment : payments) {
            generateEreceiptNumbersByOldPattern(payment, newBusiness);
        }
        paymentRepository.save(payments);
    }

    private void generateEreceiptNumbersByOldPattern(Payment payment, boolean newBusiness) {
        String policyNumber = payment.getPolicyId();
        String receiptNumberBase36 = generateReceiptNumberOldPattern(policyNumber, newBusiness);
        payment.setReceiptFullNumberBase36(receiptNumberBase36);
        payment.setReceiptNumberOldPattern(true);
        if (!newBusiness) {
            payment.setRetried(true);
        }
    }

    public static String generateReceiptNumberOldPattern(String policyNumber, boolean newBusiness) {
        String[] policyNumberParts = policyNumber.split("-");
        String policyNumberPrefix = policyNumberParts[0];
        String policyNumberSuffix = policyNumberParts[1];
        String receiptNumberBase36;
        if (newBusiness) {
            receiptNumberBase36 = policyNumberPrefix.charAt(2) + policyNumberSuffix;
        } else {
            receiptNumberBase36 = policyNumberPrefix.charAt(2) + policyNumberSuffix.substring(0, 5) + "02";
        }
        return receiptNumberBase36;
    }
}
