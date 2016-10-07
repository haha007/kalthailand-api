package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import java.util.List;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentQueryService {
    public final static Logger LOGGER = LoggerFactory.getLogger(PaymentQueryService.class);
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentQueryService(PaymentRepository paymentRepository) {this.paymentRepository = paymentRepository;}

    public Payment validateExistFirstPaymentOrderById(String policyNumber) {
        Payment firstPayment = findFirstPaymentOrderById(policyNumber);
        if (firstPayment == null) {
            throw new UnexpectedException("Something wrong. Not find any firstPayment by policyId " + policyNumber);
        }
        return firstPayment;
    }

    public Payment findFirstPaymentOrderById(String policyNumber) {
        Sort sort = new Sort(Sort.Direction.ASC, "paymentId");
        Pageable pageable = new PageRequest(0, 1, sort);

        List<Payment> payments = paymentRepository.findByPolicyId(policyNumber, pageable);
        if (payments.isEmpty()) {
            return null;
        } else {
            return payments.get(0);
        }
    }
}
