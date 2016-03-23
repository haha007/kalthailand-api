package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.exception.ElifeException;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.service.PolicyService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.PENDING_VALIDATION;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final PolicyService policyService;
    private final QuoteService quoteService;

    @Inject
    public PolicyResource(PolicyService policyService, QuoteService quoteService) {
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
    public ResponseEntity createPolicy(
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
            return new ResponseEntity<>(INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }

        Optional<Quote> tmp = quoteService.findByQuoteId(quote.getQuoteId(), sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED, NOT_FOUND);
        }

        Policy policy;
        try {
            policy = policyService.createPolicy(quote);
        } catch (ElifeException e) {
            logger.error("Unable to create a policy from the validated quote [" + jsonQuote + "]", e);
            return new ResponseEntity<>(POLICY_CANNOT_BE_CREATED.apply(e.getMessage()), NOT_ACCEPTABLE);
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
    public ResponseEntity getPolicyPayments(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }
        return new ResponseEntity<>(getJson(policy.get().getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy status", notes = "Updates the Policy status to PENDING_VALIDATION. If " +
            "susuccessful, it also generates the DA form and Application form documents. Payment will be updated to " +
            "store the registration key which will be used later on for recurrent payments.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking has failed and error details have not been provided", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment booking is successful and no registration key has been provided", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/pendingValidation", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updatePolicyToPendingValidation(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The payment ID", required = true)
            @RequestParam String paymentId,
            @ApiParam(value = "The amount registered through the channel", required = true, defaultValue = "0.0")
            @RequestParam Double value,
            @ApiParam(value = "The currency registered through the channel", required = true)
            @RequestParam String currencyCode,
            @ApiParam(value = "The registration key given by the channel (if any)", required = false)
            @RequestParam Optional<String> registrationKey,
            @ApiParam(value = "The status of the transaction through the channel", required = true)
            @RequestParam SuccessErrorStatus status,
            @ApiParam(value = "The Channel", required = true)
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The error message given by the channel (if any)", required = false)
            @RequestParam Optional<String> errorCode,
            @ApiParam(value = "The error message given by the channel (if any)", required = false)
            @RequestParam Optional<String> errorMessage) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }

        if (!policy.get().getStatus().equals(PolicyStatus.PENDING_PAYMENT)) {
            logger.error("The policy is in status [" + policy.get().getStatus().name() + "] and cannot be updated to " + PENDING_VALIDATION + " status.");
            return new ResponseEntity<>(POLICY_IS_NOT_PENDING_FOR_PAYMENT.apply(policyId), NOT_ACCEPTABLE);
        }

        Optional<Payment> payment = policy.get().getPayments().stream().filter(tmp -> tmp.getPaymentId().equals(paymentId)).findFirst();
        if (!payment.isPresent()) {
            logger.error("Unable to find the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_CONTAIN_PAYMENT, NOT_ACCEPTABLE);
        }

        if (ERROR.equals(status)) {
            if (!errorCode.isPresent() || !errorMessage.isPresent() || isEmpty(errorCode.get()) || isEmpty(errorMessage.get())) {
                return new ResponseEntity<>(PAYMENT_NOT_UPDATED_ERROR_DETAILS_NEEDED, NOT_ACCEPTABLE);
            }
        } else {
            if (!registrationKey.isPresent() || isEmpty(registrationKey.get())) {
                return new ResponseEntity<>(PAYMENT_NOT_UPDATED_REG_KEY_NEEDED, NOT_ACCEPTABLE);
            }
        }

        // Update the payment whatever the status of the payment is
        policyService.reservePayments(policy.get(), registrationKey, status, channelType, errorCode, errorMessage);

        // If in error, nothing else should be done
        if (ERROR.equals(status)) {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }

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
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 500, message = "If there was some internal error", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/update/status/validated", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updatePolicyToValidated(
            @ApiParam(value = "The policy ID", required = true)
            @PathVariable String policyId,
            @ApiParam(value = "The payment ID", required = true)
            @RequestParam String paymentId,
            @ApiParam(value = "The amount registered through the channel", required = true, defaultValue = "0.0")
            @RequestParam Double value,
            @ApiParam(value = "The currency registered through the channel", required = true)
            @RequestParam String currencyCode) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }

        Optional<Payment> payment = policy.get().getPayments().stream().filter(tmp -> tmp.getPaymentId().equals(paymentId)).findFirst();
        if (!payment.isPresent()) {
            logger.error("Unable to find the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_CONTAIN_PAYMENT, NOT_ACCEPTABLE);
        }

        //TODO : call LINE Pay API
        Optional<String> creditCardName = Optional.empty();
        Optional<String> paymentMethod = Optional.empty();
        Optional<String> errorCode = Optional.empty();
        Optional<String> errorMessage = Optional.empty();
        SuccessErrorStatus status = ERROR;
        ChannelType channelType = LINE;

        // Update the payment if confirm is success
        policyService.confirmPayment(payment.get(), value, currencyCode, status, channelType, creditCardName, paymentMethod, errorCode, errorMessage);

        //TODO get the ereceipt pdf
        try {
            policyService.updatePolicyAfterPolicyHasBeenValidated(policy.get(), null);
        } catch (ElifeException e) {
            return new ResponseEntity<>(SMS_IS_UNAVAILABLE, INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(getJson(policy.get()), OK);
    }
}
