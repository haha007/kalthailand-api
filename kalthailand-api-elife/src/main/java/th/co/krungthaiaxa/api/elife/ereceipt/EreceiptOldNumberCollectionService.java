package th.co.krungthaiaxa.api.elife.ereceipt;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class EreceiptOldNumberCollectionService {
    public static final Logger LOGGER = LoggerFactory.getLogger(EreceiptOldNumberCollectionService.class);
    //TODO we can remove this injection after Running the OldReceiptNumber in the first time.
    //This injection will make sure that the oldReceiptNumber will be generated before this class is constructed.
//    private final EreceiptOldNumberGenerationService ereceiptOldNumberGenerationService;
    private final PaymentRepository paymentRepository;

    //This class should use TreeSet so that we can use binarySort when using contains() operator.
    private Set<String> receiptFullDisplayNumbers;

    @Autowired
    public EreceiptOldNumberCollectionService(PaymentRepository paymentRepository) {
//        this.ereceiptOldNumberGenerationService = ereceiptOldNumberGenerationService;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    private void collectAllOldEreceiptNumbers() {
        Instant start = LogUtil.logStarting("Collect old receiptNumber of old payments [start]");
        List<Payment> payments = paymentRepository.findReceiptNumbersByReceiptNumberOldPatternAndReceiptNumberNotNull(true);
        logPaymentsWithOldReceiptNumbers(payments);
        receiptFullDisplayNumbers = payments.stream().map(payment -> payment.getReceiptNumber().getFullNumberForDisplay()).collect(Collectors.toCollection(TreeSet<String>::new));
        receiptFullDisplayNumbers = UnmodifiableSet.decorate(receiptFullDisplayNumbers);
        LogUtil.logFinishing(start, "Collect old receiptNumber of old payments [finish]");
    }

    private void logPaymentsWithOldReceiptNumbers(List<Payment> payments) {
        if (!LOGGER.isDebugEnabled()) return;
        String result = payments.stream().map(payment -> toStringPaymentWithOldReceiptNumber(payment)).collect(Collectors.joining("\n"));
        LOGGER.debug("Old payments with old receiptNumbers:\n[\n" + result + "\n]");
    }

    private String toStringPaymentWithOldReceiptNumber(Payment payment) {
        return String.format("ReceiptNumber: %s\t, PolicyId: %s\t, PaymentId: %s", "" + payment.getReceiptNumber(), payment.getPolicyId(), payment.getPaymentId());
    }

    /**
     * @param ereceiptFullDisplayNumber the full number in base36 format. (Doesn't include {@link EreceiptPdfService#ERECEIPT_NUMBER_PREFIX}).
     * @return
     */
    public boolean checkDuplicateIncrementalInOldData(String ereceiptFullDisplayNumber) {
        return this.receiptFullDisplayNumbers.contains(ereceiptFullDisplayNumber);
    }
}
