package th.co.krungthaiaxa.api.elife.service;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponsePaymentInfo;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode.FAKE_WITH_ERROR;
import static th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode.FAKE_WITH_SUCCESS;
import static th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode.REAL;

/**
 * @author khoi.tran on 10/4/16.
 *         Note: this service is not for validate the input data of policy.
 *         This service changes the policy status from Pendi{@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus#PENDING_VALIDATION}ng to {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus#VALIDATED}.
 *         View more at {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus}
 */
@Service
public class PolicyValidatedProcessingService {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyValidatedProcessingService.class);

    @Value("${environment.name}")
    private String environmentName;

    private final PolicyService policyService;
    private final BeanValidator beanValidator;
    private LineService lineService;

    @Autowired
    public PolicyValidatedProcessingService(PolicyService policyService, BeanValidator beanValidator) {
        this.policyService = policyService;
        this.beanValidator = beanValidator;
    }

    public Policy processValidatedPolicy(PolicyValidationRequest policyValidationRequest) {
        beanValidator.validate(policyValidationRequest);

        String agentCode = policyValidationRequest.getAgentCode();
        String policyId = policyValidationRequest.getPolicyId();
        String agentName = policyValidationRequest.getAgentName();
        String accessToken = policyValidationRequest.getAccessToken();
        LinePayCaptureMode linePayCaptureMode = policyValidationRequest.getLinePayCaptureMode();

        if (environmentName.equals("PRD") && !linePayCaptureMode.equals(REAL)) {
            throw new BaseException(ErrorCode.ERROR_CODE_REAL_CAPTURE_API_HAS_TO_BE_USED, "The environment is production, so you have to choose the real payment option.");
        }

        Policy policy = policyService.validateExistPolicy(policyId);

        Optional<Payment> paymentOptional = policy.getPayments()
                .stream()
                .filter(tmp -> tmp.getTransactionId() != null)
                .findFirst();
        if (!paymentOptional.isPresent()) {
            LOGGER.error("Unable to find a payment with a transaction id pending for confirmation in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID), NOT_ACCEPTABLE);
        }

        Payment payment = paymentOptional.get();
        LinePayResponse linePayResponse = null;
        if (linePayCaptureMode.equals(REAL)) {
            LOGGER.info("Will try to confirm payment with ID [" + payment.getPaymentId() + "] and transation ID [" + payment.getTransactionId() + "] on the policy with ID [" + policyId + "]");
            try {
                linePayResponse = lineService.capturePayment(payment.getTransactionId(), payment.getAmount().getValue(), payment.getAmount().getCurrencyCode());
            } catch (RuntimeException | IOException e) {
                LOGGER.error("Unable to confirm the payment in the policy with ID [" + policyId + "]", e);
                return new ResponseEntity<>(getJson(ErrorCode.UNABLE_TO_CAPTURE_PAYMENT.apply(e.getMessage())), NOT_ACCEPTABLE);
            }
        } else if (linePayCaptureMode.equals(FAKE_WITH_ERROR)) {
            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode("9999");
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with an error as a result");
        } else if (linePayCaptureMode.equals(FAKE_WITH_SUCCESS)) {
            LinePayResponsePaymentInfo payResponsePaymentInfo = new LinePayResponsePaymentInfo();
            payResponsePaymentInfo.setMethod("someMethodAfterFakeCallToLinePayCaptureAPI");
            payResponsePaymentInfo.setCreditCardName("someCreditCardNameAfterFakeCallToLinePayCaptureAPI");

            LinePayResponseInfo info = new LinePayResponseInfo();
            info.setRegKey("someRegistrationKeyAfterFakeCallToLinePayCaptureAPI");
            info.addPayInfo(payResponsePaymentInfo);

            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode(LineService.RESPONSE_CODE_SUCCESS);
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with success");
            linePayResponse.setInfo(info);
        }

        if (linePayResponse == null) {
            return new ResponseEntity<>(getJson(ErrorCode.UNABLE_TO_CAPTURE_PAYMENT.apply("No way to call Line Pay capture API has been provided")), NOT_ACCEPTABLE);
        } else if (!linePayResponse.getReturnCode().equals(LineService.RESPONSE_CODE_SUCCESS)) {
            String msg = "Confirming payment didn't go through. Error code is [" + linePayResponse.getReturnCode() + "], error message is [" + linePayResponse.getReturnMessage() + "]";
            return new ResponseEntity<>(getJson(ErrorCode.UNABLE_TO_CAPTURE_PAYMENT.apply(msg)), NOT_ACCEPTABLE);
        }

        // Update the payment if confirm is success
        policyService.updatePayment(payment, payment.getAmount().getValue(), payment.getAmount().getCurrencyCode(), LINE, linePayResponse);
        policyService.updateRegistrationForAllNotProcessedPayment(policy, linePayResponse.getInfo().getRegKey());

        try {
            policyService.updatePolicyAfterPolicyHasBeenValidated(policy, agentCode, agentName, accessToken);
        } catch (ElifeException e) {
            LOGGER.error("Payment is successful but there was an error whil trying to update policy status.", e);
            return new ResponseEntity<>(getJson(ErrorCode.POLICY_VALIDATION_ERROR.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(getJson(policy), OK);
    }

    public static class PolicyValidationRequest {
        @NotEmpty
        private String policyId;
        @NotEmpty
        @javax.validation.constraints.Pattern(regexp = "[0-9]{6}-[0-9]{2}-[0-9]{6}$", message = "AgentCode must follow format '123456-12-123456'")
        private String agentCode;
        private String agentName;
        @NotNull
        private LinePayCaptureMode linePayCaptureMode;
        @NotEmpty
        private String accessToken;

        public PolicyValidationRequest() {}

        public PolicyValidationRequest(String policyId, String agentCode, String agentName, LinePayCaptureMode linePayCaptureMode, String accessToken) {
            this.policyId = policyId;
            this.agentCode = agentCode;
            this.agentName = agentName;
            this.linePayCaptureMode = linePayCaptureMode;
            this.accessToken = accessToken;
        }

        public String getPolicyId() {
            return policyId;
        }

        public void setPolicyId(String policyId) {
            this.policyId = policyId;
        }

        public String getAgentCode() {
            return agentCode;
        }

        public void setAgentCode(String agentCode) {
            this.agentCode = agentCode;
        }

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public LinePayCaptureMode getLinePayCaptureMode() {
            return linePayCaptureMode;
        }

        public void setLinePayCaptureMode(LinePayCaptureMode linePayCaptureMode) {
            this.linePayCaptureMode = linePayCaptureMode;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
