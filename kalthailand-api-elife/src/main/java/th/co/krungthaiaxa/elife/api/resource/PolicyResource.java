package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.service.DocumentService;
import th.co.krungthaiaxa.elife.api.service.EmailService;
import th.co.krungthaiaxa.elife.api.service.PolicyService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final PolicyService policyService;
    private final QuoteService quoteService;
    private final EmailService emailService;
    private final DocumentService documentService;

    @Inject
    public PolicyResource(PolicyService policyService, QuoteService quoteService, EmailService emailService, DocumentService documentService) {
        this.policyService = policyService;
        this.quoteService = quoteService;
        this.emailService = emailService;
        this.documentService = documentService;
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
        } catch (Exception e) {
            logger.error("Unable to create a policy from the validated quote [" + jsonQuote + "]", e);
            return new ResponseEntity<>(POLICY_CANNOT_BE_CREATED.apply(e.getMessage()), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy), OK);
    }

    @ApiOperation(value = "Policy payments", notes = "Get the payments of a policy", response = Payment.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getPolicyPayments(
            @ApiParam(value = "The policy ID")
            @PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            logger.error("Unable to find the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy.get().getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy payment and documents", notes = "Generates Policy documents and updates a " +
            "specific payment of a Policy. Fields 'payment effective date' and 'payment status' will be calculated " +
            "according to the given status and the amount compare to the amount expected.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment has failed and no error details have been provided", response = Error.class),
            @ApiResponse(code = 500, message = "If the payment has not been updated", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments/{paymentId}", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updatePolicyPayment(
            @ApiParam(value = "The policy ID")
            @PathVariable String policyId,
            @ApiParam(value = "The payment ID")
            @PathVariable String paymentId,
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
            @ApiParam(value = "The credit card name given by the channel (if any)", required = false)
            @RequestParam Optional<String> creditCardName,
            @ApiParam(value = "The payment method given by the channel (if any)", required = false)
            @RequestParam Optional<String> paymentMethod,
            @ApiParam(value = "The error message given by the channel (if any)", required = false)
            @RequestParam Optional<String> errorCode,
            @ApiParam(value = "The error message given by the channel (if any)", required = false)
            @RequestParam Optional<String> errorMessage) {
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

        if (ERROR.equals(status)) {
            if (!errorCode.isPresent() || !errorMessage.isPresent() || isEmpty(errorCode.get()) || isEmpty(errorMessage.get())) {
                return new ResponseEntity<>(PAYMENT_NOT_UPDATED_ERROR_DETAILS_NEEDED, NOT_ACCEPTABLE);
            }
        }

        // Update the policy
        try {
            policyService.updatePayment(policy.get(), payment.get(), value, currencyCode, registrationKey, status,
                    channelType, creditCardName, paymentMethod, errorCode, errorMessage);
        } catch (IOException e) {
            logger.error("Unable to update the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(PAYMENT_NOT_UPDATED, INTERNAL_SERVER_ERROR);
        }
        policyService.addAgentCodes(policy.get());

        // Generate documents
        documentService.generatePolicyDocuments(policy.get());

        // Get Ereceipt
        Optional<Document> documentPdf = policy.get().getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();

        // Send Email
        if (documentPdf.isPresent()) {
            DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
            try {
                emailService.sendEreceiptEmail(policy.get(), Pair.of(Base64.getDecoder().decode(documentDownload.getContent()), "e-receipt_" + policy.get().getPolicyId() + ".pdf"));
            } catch (Exception e) {
                logger.error(String.format("Unable to send e-receipt document while sending email with policy id is [%1$s].", policy.get().getPolicyId()), e);
                return new ResponseEntity<>(UNABLE_TO_SEND_EMAIL, INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.error(String.format("E-receipt of policy [%1$s] is not available.", policy.get().getPolicyId()));
            return new ResponseEntity<>(UNABLE_TO_CREATE_ERECEIPT, INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(JsonUtil.getJson(policy.get()), OK);
    }

}
