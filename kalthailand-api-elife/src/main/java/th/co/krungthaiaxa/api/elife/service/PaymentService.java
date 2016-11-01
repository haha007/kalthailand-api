package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.exception.PaymentHasNewerCompletedException;
import th.co.krungthaiaxa.api.elife.exception.PaymentNotFoundException;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.api.elife.model.line.BaseLineResponse;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.COMPLETED;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.INCOMPLETE;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class PaymentService {
    public final static Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    /**
     * Need getter-setter for mocking.
     */
    private LineService lineService;
    private final PaymentRepository paymentRepository;

    @Inject
    public PaymentService(PaymentRepository paymentRepository, LineService lineService) {
        this.paymentRepository = paymentRepository;
        this.lineService = lineService;
    }

    //TODO should move to {@link PaymentQueryService}
    public Payment findFirstPaymentHasTransactionId(String policyNumber) {
        Pageable pageable = new PageRequest(0, 1);
        List<Payment> payments = paymentRepository.findByPolicyIdAndTransactionIdNotNull(policyNumber, pageable);
        if (payments.isEmpty()) {
            return null;
        } else {
            return payments.get(0);
        }
    }

    public Optional<Payment> findLastestPaymentByPolicyNumberAndRegKeyNotNull(String policyNumber) {
        return paymentRepository.findOneByRegKeyNotNullAndPolicyId(policyNumber, new Sort(Sort.Direction.DESC, "dueDate"));
    }

    public Payment findPaymentById(String paymentId) {
        return paymentRepository.findOne(paymentId);
    }

    /**
     * Find closest newer payment which was competed.
     * For example:
     * payment 1: INCOMPLETED
     * payment 2: COMPLETED
     * payment 3: COMPLETED
     * <p>
     * result findNewerCompletedPayment(payment1) is payment2, not payment3
     *
     * @param oldPaymentId
     * @return
     */
    public PaymentNewerCompletedResult findNewerCompletedPaymentInSamePolicy(String oldPaymentId) {
        PaymentNewerCompletedResult result = new PaymentNewerCompletedResult();
        Payment oldPayment = validateExistPayment(oldPaymentId);
        result.setPayment(oldPayment);
        Payment newerCompletedPayment;
        if (oldPayment.getRetryPaymentId() != null) {
            Payment retryPayment = validateExistPayment(oldPayment.getRetryPaymentId());
            if (PaymentStatus.COMPLETED.equals(retryPayment.getStatus())) {
                result.setNewerCompletedPayment(retryPayment);
                return result;
            }
        }

        LocalDateTime oldEffectiveDate = oldPayment.getEffectiveDate();
        if (oldEffectiveDate != null) {
            newerCompletedPayment = paymentRepository.findOneByPolicyAndNewerEffectiveDate(oldPayment.getPolicyId(), oldEffectiveDate, PaymentStatus.COMPLETED);
        } else {
            LOGGER.warn("Something wrong: The old payment must be processed, so it must have effective date. But we cannot find effectiveDate of this paymentId: " + oldPayment.getPaymentId());
            newerCompletedPayment = paymentRepository.findOneByPolicyAndNewerId(oldPayment.getPaymentId(), oldPayment.getPaymentId(), PaymentStatus.COMPLETED);
        }
        result.setNewerCompletedPayment(newerCompletedPayment);
        return result;
    }

    public Payment validateNotExistNewerPayment(String paymentId) {
        PaymentNewerCompletedResult paymentNewerCompletedResult = findNewerCompletedPaymentInSamePolicy(paymentId);
        Payment oldPayment = paymentNewerCompletedResult.getPayment();
        Payment newerCompletedPayment = paymentNewerCompletedResult.getNewerCompletedPayment();
        if (newerCompletedPayment != null) {
            throw new PaymentHasNewerCompletedException(newerCompletedPayment, String.format("There's a newer payment which was completed: old payment Id: %s. Newer completed paymentId: %s", oldPayment.getPaymentId(), newerCompletedPayment.getPaymentId()));
        }
        return oldPayment;
    }

    /**
     * @param payment
     * @param linePayResponse
     * @return
     */
    public Payment updateByLinePayResponse(Payment payment, BaseLineResponse linePayResponse) {

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setRejectionErrorCode(linePayResponse.getReturnCode());
        paymentInformation.setRejectionErrorMessage(linePayResponse.getReturnMessage());
        if (linePayResponse.getReturnCode().equals(LineService.RESPONSE_CODE_SUCCESS)) {
            String msg = "Success payment " + ObjectMapperUtil.toString(payment) + ". Response: " + ObjectMapperUtil.toString(linePayResponse);
            paymentInformation.setStatus(SuccessErrorStatus.SUCCESS);
            paymentInformation.setMethod(msg);
            payment.setStatus(COMPLETED);
        } else {
            payment.setStatus(INCOMPLETE);
        }
        payment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        payment.addPaymentInformation(paymentInformation);
        return paymentRepository.save(payment);
    }

    public Payment validateExistPayment(String paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("Not found payment with Id " + paymentId);
        }
        return payment;
    }

    public LineService getLineService() {
        return lineService;
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
