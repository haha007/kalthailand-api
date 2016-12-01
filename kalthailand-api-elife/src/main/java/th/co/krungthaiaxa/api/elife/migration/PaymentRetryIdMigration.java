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
 * @author khoi.tran on 12/1/16.
 */
@Service
public class PaymentRetryIdMigration {
    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentRetryIdMigration.class);
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentRetryIdMigration(PaymentRepository paymentRepository) {this.paymentRepository = paymentRepository;}

    @PostConstruct
    public void init() {
        insertFieldRetryFromPreviousPaymentId();
    }

    private void insertFieldRetryFromPreviousPaymentId() {
        ActionLoopByPage actionLoopByPage = new ActionLoopByPage<Payment>() {
            @Override
            protected List<Payment> executeEachPageData(Pageable pageRequest) {
                List<Payment> originalPayments = paymentRepository.findByRetryPaymentIdNotNull(pageRequest);
                List<Payment> retryPayments = new ArrayList<>();
                for (Payment originalPayment : originalPayments) {
                    String retryPaymentId = originalPayment.getRetryPaymentId();
                    Payment retryPayment = paymentRepository.findOne(retryPaymentId);
                    if (retryPayment == null) {
                        LOGGER.warn("Not found retryPaymentId " + retryPaymentId + ", previousPayment: " + originalPayment.getPaymentId());
                    } else {
                        retryPayment.setRetryFromPreviousPaymentId(originalPayment.getPaymentId());
                        retryPayments.add(retryPayment);
                    }
                }
                paymentRepository.save(retryPayments);
                return originalPayments;
            }
        };
        actionLoopByPage.executeAllPages(20);
    }

}
