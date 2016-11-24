package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.exception.PaymentHasNewerCompletedException;
import th.co.krungthaiaxa.api.elife.exception.PaymentNotFoundException;
import th.co.krungthaiaxa.api.elife.line.LineService;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.api.elife.model.line.BaseLineResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponsePaymentInfo;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.COMPLETED;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.INCOMPLETE;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.OVERPAID;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amount;

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
        Sort sort = new Sort(Sort.Direction.ASC, "dueDate");
        Pageable pageable = new PageRequest(0, 1, sort); //TODO need to test short by dueDate
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

    public Payment validateExistPayment(String paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("Not found payment with Id " + paymentId);
        }
        return payment;
    }

    public Payment validateExistPaymentInPolicy(String policyId, String paymentId) {
        Payment payment = validateExistPayment(paymentId);
        if (!policyId.equals(payment.getPolicyId())) {
            String message = "The payment [" + paymentId + "] has policyId [" + payment.getPolicyId() + "], which is different from required the policy [" + policyId + "]";
            throw new PaymentNotFoundException(message);
        }
        return payment;
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

    public Payment updatePayment(Payment payment, String orderId, String transactionId, String regKey) {
        payment.setTransactionId(transactionId);
        payment.setOrderId(orderId);
        payment.setRegistrationKey(regKey);
        Payment result = paymentRepository.save(payment);
        LOGGER.info("Payment [" + payment.getPaymentId() + "] has been booked with transactionId [" + payment.getTransactionId() + "]");
        LOGGER.debug("Saved payment: " + ObjectMapperUtil.toStringMultiLine(payment));
        return result;
    }

    public void updatePaymentWithErrorStatus(Payment payment, Double amount, String currencyCode, ChannelType channelType,
            String errorCode, String errorMessage) {
        updatePaymentWithPaylineResponse(payment, amount, currencyCode, channelType,
                errorCode,
                errorMessage,
                null,
                null,
                null);
    }

    public void updatePaymentAfterLinePay(Payment payment, Double amount, String currencyCode, ChannelType channelType,
            LinePayResponse linePayResponse) {
        String creditCardName = null;
        String method = null;
        LinePayResponseInfo linePayResponseInfo = linePayResponse.getInfo();
        String regKey = linePayResponseInfo.getRegKey();
        regKey = StringUtils.isNotBlank(regKey) ? regKey : null;
        List<LinePayResponsePaymentInfo> linePayResponsePaymentInfoList = linePayResponseInfo.getPayInfo();
        if (!linePayResponsePaymentInfoList.isEmpty()) {
            LinePayResponsePaymentInfo linePayResponsePaymentInfo = linePayResponsePaymentInfoList.get(0);
            creditCardName = linePayResponsePaymentInfo.getCreditCardName();
            method = linePayResponsePaymentInfo.getMethod();
        }

        // Update the confirmed payment
        updatePaymentWithPaylineResponse(payment, amount, currencyCode, channelType,
                linePayResponse.getReturnCode(),
                linePayResponse.getReturnMessage(),
                regKey,
                creditCardName,
                method);
    }

    private void updatePaymentWithPaylineResponse(Payment payment, Double value, String currencyCode, ChannelType channelType, String errorCode, String errorMessage, String registrationKey, String creditCardName, String paymentMethod) {
        SuccessErrorStatus paymentInformationStatus;
        if (!currencyCode.equals(payment.getAmount().getCurrencyCode())) {
            paymentInformationStatus = SuccessErrorStatus.ERROR;
            errorMessage = "Currencies are different";
            errorCode = LineService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
        } else if (!isEmpty(errorCode) && !errorCode.equals("0000")) {
            paymentInformationStatus = SuccessErrorStatus.ERROR;
        } else {
            paymentInformationStatus = SuccessErrorStatus.SUCCESS;
        }

        // registration key might have to be updated
        if (!isBlank(registrationKey) && !registrationKey.equals(payment.getRegistrationKey())) {
            payment.setRegistrationKey(registrationKey);
        }
        LocalDate nowInThai = DateTimeUtil.nowLocalDateInThaiZoneId();
        LocalDateTime nowDateTimeInThai = DateTimeUtil.nowLocalDateTimeInThaiZoneId();

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setAmount(amount(value, currencyCode));
        paymentInformation.setChannel(channelType);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setDate(nowInThai);
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setRejectionErrorCode(errorCode);
        paymentInformation.setRejectionErrorMessage(errorMessage);
        paymentInformation.setStatus(paymentInformationStatus);
        payment.getPaymentInformations().add(paymentInformation);

        Double totalSuccesfulPayments = payment.getPaymentInformations()
                .stream()
                .filter(tmp -> tmp.getStatus() != null && tmp.getStatus().equals(SuccessErrorStatus.SUCCESS))
                .mapToDouble(ipaymentInformation -> getAmountOfPaymentInformation(ipaymentInformation))
                .sum();
        if (totalSuccesfulPayments < payment.getAmount().getValue()) {
            payment.setStatus(INCOMPLETE);
        } else if (Objects.equals(totalSuccesfulPayments, payment.getAmount().getValue())) {
            payment.setStatus(COMPLETED);
        } else if (totalSuccesfulPayments > payment.getAmount().getValue()) {
            payment.setStatus(OVERPAID);
        }
        payment.setEffectiveDate(nowDateTimeInThai);
        paymentRepository.save(payment);
        LOGGER.info("Payment [" + payment.getPaymentId() + "] has been updated");
    }

    private double getAmountOfPaymentInformation(PaymentInformation paymentInformation) {
        if (paymentInformation == null || paymentInformation.getAmount() == null || paymentInformation.getAmount().getValue() == null) {
            return 0.0;
        } else {
            return paymentInformation.getAmount().getValue();
        }
    }

    public LineService getLineService() {
        return lineService;
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }

    /**
     * TODO same as {@link PaymentService#updateByLinePayResponse(Payment, BaseLineResponse)} .
     */
    public Payment updatePaymentWithLineResponse(Payment payment, ChannelType line, LinePayResponse linePayResponse) {
        Instant start = LogUtil.logStarting("updatePaymentWithLineResponse. paymentId: " + payment.getPaymentId() + ", policyId: " + payment.getPolicyId());
        //Same as {@link #updatePaymentWithPaylineResponse(...)}
        String creditCardName = null;
        String method = null;
        LinePayResponseInfo linePayResponseInfo = linePayResponse.getInfo();
        List<LinePayResponsePaymentInfo> linePayResponsePaymentInfoList = linePayResponseInfo.getPayInfo();
        if (!linePayResponsePaymentInfoList.isEmpty()) {
            LinePayResponsePaymentInfo linePayResponsePaymentInfo = linePayResponsePaymentInfoList.get(0);
            creditCardName = linePayResponsePaymentInfo.getCreditCardName();
            method = linePayResponsePaymentInfo.getMethod();
        }

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setChannel(line);
        paymentInformation.setCreditCardName(creditCardName);
        paymentInformation.setMethod(method);

        //Same as {@link PaymentService#updateByLinePayResponse(Payment, BaseLineResponse)}
        paymentInformation.setRejectionErrorCode(linePayResponse.getReturnCode());
        paymentInformation.setRejectionErrorMessage(linePayResponse.getReturnMessage());
        if (LineService.RESPONSE_CODE_SUCCESS.equals(linePayResponse.getReturnCode())) {
            String msg = "Success payment " + ObjectMapperUtil.toString(payment) + ". Response: " + ObjectMapperUtil.toString(linePayResponse);
            paymentInformation.setStatus(SuccessErrorStatus.SUCCESS);
            paymentInformation.setRejectionErrorMessage(msg);
            payment.setStatus(COMPLETED);
        } else {
            payment.setStatus(INCOMPLETE);
        }
        payment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        payment.addPaymentInformation(paymentInformation);
        //NOTE: Never get the regKey from linePayResponse and set to payment because the result from linePayResponse is always null. So it can remove the previous regKey of payment.
        payment = paymentRepository.save(payment);
        LogUtil.logRuntime(start, "updatePaymentWithLineResponse. paymentId: " + payment.getPaymentId() + ", policyId: " + payment.getPolicyId());
        return payment;
    }
}
