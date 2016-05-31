package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponsePaymentInfo;
import th.co.krungthaiaxa.api.elife.service.LineService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.PENDING_VALIDATION;
import static th.co.krungthaiaxa.api.elife.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode.*;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final LineService lineService;
    private final PolicyService policyService;
    private final QuoteService quoteService;

    @Value("${environment.name}")
    private String environmentName;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    @Inject
    public PolicyResource(LineService lineService, PolicyService policyService, QuoteService quoteService) {
        this.lineService = lineService;
        this.policyService = policyService;
        this.quoteService = quoteService;
    }

    @ApiOperation(value = "Creates a policy", notes = "Creates a policy out of a quote. Policy will be created only " +
            "if it went through all product validation. Policy will also contain calculated payment schedule",
            response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If JSon of quote is invalid or if Policy could not be created",
                    response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> createPolicy(
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The json of the quote to create the policy from. This quote will go through maximum " +
                    "validations")
            @RequestBody String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(INVALID_QUOTE_PROVIDED), NOT_ACCEPTABLE);
        }

        Optional<Quote> tmp = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(getJson(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED), NOT_FOUND);
        }

        Policy policy;
        try {
            policy = policyService.createPolicy(quote);
        } catch (ElifeException e) {
            logger.error("Unable to create a policy from the validated quote [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(POLICY_CANNOT_BE_CREATED.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(getJson(policy), OK);
    }

    @ApiOperation(value = "Policy payments", notes = "Get the payments of a policy", response = Payment.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicyPayments(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }
        return new ResponseEntity<>(getJson(policy.get().getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to PENDING_VALIDATION. If " +
            "susccessful, it also generates the DA form and Application form documents. Payment will be updated to " +
            "store the registration key which will be used later on for recurrent payments. Payment will also store " +
            "orderId and transactionId for trackign purprose.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking has failed and error details have not been provided", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking is successful and no registration key has been provided", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/pendingValidation", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity<byte[]> updatePolicyToPendingValidation(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The payment ID", required = true)
            @RequestParam String paymentId,
            @ApiParam(value = "The order id used to book the payment", required = true)
            @RequestParam String orderId,
            @ApiParam(value = "The transaction id to use to confirm the payment. Must be sent of status id SUCCESS", required = false)
            @RequestParam(required = false) Optional<String> transactionId) {
        if (isEmpty(orderId)) {
            logger.error("The order ID was not received");
            return new ResponseEntity<>(getJson(ORDER_ID_NOT_PROVIDED), NOT_ACCEPTABLE);
        }

        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        if (!policy.get().getStatus().equals(PolicyStatus.PENDING_PAYMENT)) {
            logger.error("The policy is in status [" + policy.get().getStatus().name() + "] and cannot be updated to " + PENDING_VALIDATION + " status.");
            return new ResponseEntity<>(getJson(POLICY_IS_NOT_PENDING_FOR_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        }

        Optional<Payment> payment = policy.get().getPayments().stream().filter(tmp -> tmp.getPaymentId().equals(paymentId)).findFirst();
        if (!payment.isPresent()) {
            logger.error("Unable to find the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID), NOT_ACCEPTABLE);
        }

        // If no transaction id, then in error, nothing else should be done since we don't have a status (error / success)
        if (!transactionId.isPresent() || isEmpty(transactionId.get())) {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }

        // Update the payment
        policyService.updatePayment(payment.get(), orderId, transactionId.get());
        // Update the policy status
        policyService.updatePolicyAfterFirstPaymentValidated(policy.get());

        return new ResponseEntity<>(getJson(policy.get()), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to VALIDATED. If " +
            "susuccessful, it also generates the eReceipt form document (image and PDF) and eReceipt pdf is sent to " +
            "Tele sale API. LINE Pay API is called to confirm the payment booking made earlier using the " +
            "registration key. Payment will be updated with amount and effective date. Finally, it sends " +
            "notifications through email, SMS and LINE push notifications", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the agent code is not in format '123456-12-123456'", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/validated", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity<byte[]> updatePolicyToValidated(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The code of validating agent", required = true)
            @RequestParam String agentCode,
            @ApiParam(value = "The name of validating agent", required = true)
            @RequestParam String agentName,
            @ApiParam(value = "The type of call to Line Pay Capture API", required = true)
            @RequestParam LinePayCaptureMode linePayCaptureMode,
            HttpServletRequest httpServletRequest) {

        Pattern pattern = Pattern.compile("[0-9]{6}-[0-9]{2}-[0-9]{6}$");
        Matcher matcher = pattern.matcher(agentCode);
        if (!matcher.find()) {
            logger.error("Agent code [" + policyId + "] is not following format '123456-12-123456'.");
            return new ResponseEntity<>(getJson(AGENT_CODE_FORMAT_ERROR), NOT_ACCEPTABLE);
        }

        if (environmentName.equals("PRD") && !linePayCaptureMode.equals(REAL)) {
            return new ResponseEntity<>(getJson(REAL_CAPTURE_API_HAS_TO_BE_USED), NOT_ACCEPTABLE);
        }

        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        Optional<Payment> paymentOptional = policy.get().getPayments()
                .stream()
                .filter(tmp -> tmp.getTransactionId() != null)
                .findFirst();
        if (!paymentOptional.isPresent()) {
            logger.error("Unable to find a payment with a transaction id pending for confirmation in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID), NOT_ACCEPTABLE);
        }

        Payment payment = paymentOptional.get();
        LinePayResponse linePayResponse = null;
        if (linePayCaptureMode.equals(REAL)) {
            logger.info("Will try to confirm payment with ID [" + payment.getPaymentId() + "] and transation ID [" + payment.getTransactionId() + "] on the policy with ID [" + policyId + "]");
            try {
                linePayResponse = lineService.capturePayment(payment.getTransactionId(), payment.getAmount().getValue(), payment.getAmount().getCurrencyCode());
            } catch (RuntimeException | IOException e) {
                logger.error("Unable to confirm the payment in the policy with ID [" + policyId + "]", e);
                return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply(e.getMessage())), NOT_ACCEPTABLE);
            }
        }
        else if (linePayCaptureMode.equals(FAKE_WITH_ERROR)) {
            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode("9999");
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with an error as a result");
        }
        else if (linePayCaptureMode.equals(FAKE_WITH_SUCCESS)) {
            LinePayResponsePaymentInfo payResponsePaymentInfo = new LinePayResponsePaymentInfo();
            payResponsePaymentInfo.setMethod("someMethodAfterFakeCallToLinePayCaptureAPI");
            payResponsePaymentInfo.setCreditCardName("someCreditCardNameAfterFakeCallToLinePayCaptureAPI");

            LinePayResponseInfo info = new LinePayResponseInfo();
            info.setRegKey("someRegistrationKeyAfterFakeCallToLinePayCaptureAPI");
            info.addPayInfo(payResponsePaymentInfo);

            linePayResponse = new LinePayResponse();
            linePayResponse.setReturnCode("0000");
            linePayResponse.setReturnMessage("This is a fake call to Line Pay API with success");
            linePayResponse.setInfo(info);
        }

        if (linePayResponse == null) {
            return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply("No way to call Line Pay capture API has been provided")), NOT_ACCEPTABLE);
        }
        else if (!linePayResponse.getReturnCode().equals("0000")) {
            String msg = "Confirming payment didn't go through. Error code is [" + linePayResponse.getReturnCode() + "], error message is [" + linePayResponse.getReturnMessage() + "]";
            return new ResponseEntity<>(getJson(UNABLE_TO_CAPTURE_PAYMENT.apply(msg)), NOT_ACCEPTABLE);
        }

        // Update the payment if confirm is success
        policyService.updatePayment(payment, payment.getAmount().getValue(), payment.getAmount().getCurrencyCode(), LINE, linePayResponse);
        policyService.updateRegistrationForAllNotProcessedPayment(policy.get(), linePayResponse.getInfo().getRegKey());

        try {
            policyService.updatePolicyAfterPolicyHasBeenValidated(policy.get(), agentCode, agentName, httpServletRequest.getHeader(tokenHeader));
        } catch (ElifeException e) {
            logger.error("Payment is successful but there was an error whil trying to update policy status.", e);
            return new ResponseEntity<>(getJson(POLICY_VALIDATION_ERROR.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(getJson(policy.get()), OK);
    }
}
