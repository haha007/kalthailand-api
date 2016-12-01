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
                        String originalPaymentId = findRecursiveOriginalPaymentId(retryPayment);
                        retryPayment.setRetryFromPreviousPaymentId(paymentWhichWasRetried.getPaymentId());
                        retryPayment.setRetryFromOriginalPaymentId(originalPaymentId);
                        retryPayments.add(retryPayment);
                    }
                }
                paymentRepository.save(retryPayments);
                return paymentsWithWasRetried;
            }
        };
        actionLoopByPage.executeAllPages(PROCESS_PAGE_SIZE);
    }

    private String findRecursiveOriginalPaymentId(Payment payment) {
        String originalPaymentId = null;
        Payment currentPayment = payment;
        Payment previousPayment;
        do {
            previousPayment = paymentRepository.findOneByRetryPaymentId(currentPayment.getPaymentId());
            if (previousPayment != null) {
                originalPaymentId = previousPayment.getPaymentId();
                currentPayment = previousPayment;
            }
        } while (previousPayment != null);
        return originalPaymentId;
    }
}
