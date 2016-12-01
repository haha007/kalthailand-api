package th.co.krungthaiaxa.api.elife.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.action.ActionLoopByPage;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Version 1.11.0
 *
 * @author khoi.tran on 12/1/16.
 */
@Service
public class PreviousPaymentIdMigration {
    public static final Logger LOGGER = LoggerFactory.getLogger(PreviousPaymentIdMigration.class);
    public static final int PROCESS_PAGE_SIZE = 20;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PreviousPaymentIdMigration(PaymentRepository paymentRepository) {this.paymentRepository = paymentRepository;}

    @PostConstruct
    public void init() {
        insertFieldRetryFromPreviousPaymentId();
    }

    private void insertFieldRetryFromPreviousPaymentId() {
        ActionLoopByPage actionLoopByPage = new ActionLoopByPage<Payment>("PreviousPaymentIdMigration") {
            @Override
            protected List<Payment> executeEachPageData(Pageable pageRequest) {
                List<Payment> paymentsWithWasRetried = paymentRepository.findByRetryPaymentIdNotNull(pageRequest);
                List<Payment> retryPayments = new ArrayList<>();
                for (Payment paymentWhichWasRetried : paymentsWithWasRetried) {
                    String retryPaymentId = paymentWhichWasRetried.getRetryPaymentId();
                    Payment retryPayment = paymentRepository.findOne(retryPaymentId);
                    if (retryPayment == null) {
                        LOGGER.warn("Not found retryPaymentId " + retryPaymentId + ", previousPayment: " + paymentWhichWasRetried.getPaymentId());
                    } else {
                        Payment originalPayment = findRecursiveOriginalPaymentId(retryPayment);
                        if (originalPayment != null) {
                            retryPayment.setDueDate(originalPayment.getDueDate());
                            retryPayment.setRetryFromOriginalPaymentId(originalPayment.getPaymentId());
                        }
                        retryPayment.setRetryFromPreviousPaymentId(paymentWhichWasRetried.getPaymentId());
                        retryPayments.add(retryPayment);
                    }
                }
                paymentRepository.save(retryPayments);
                return paymentsWithWasRetried;
            }
        };
        actionLoopByPage.executeAllPages(PROCESS_PAGE_SIZE);
    }

    private Payment findRecursiveOriginalPaymentId(Payment payment) {
        Payment originalPayment = null;
        Payment currentPayment = payment;
        Payment previousPayment;
        do {
            previousPayment = paymentRepository.findOneByRetryPaymentId(currentPayment.getPaymentId());
            if (previousPayment != null) {
                originalPayment = previousPayment;
                currentPayment = previousPayment;
            }
        } while (previousPayment != null);
        return originalPayment;
    }
}
