package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.*;
import th.co.krungthaiaxa.elife.api.model.enums.*;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.service.EmailService;
import th.co.krungthaiaxa.elife.api.service.PolicyService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;
import th.co.krungthaiaxa.elife.api.utils.ImageUtil;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static java.lang.Boolean.FALSE;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final static String ERECEIPT_PDF_FILE_NAME= "ereceipt.pdf";
    @Value("${path.store.watermarked.image}")
    private String storePath;
    private final PolicyService policyService;
    @Value("${path.store.elife.ereceipt.pdf}")
    private String eReceiptPdfStorePath;
    private final EmailService emailService;
    private final QuoteService quoteService;

    @Inject
    public PolicyResource(PolicyService policyService, EmailService emailService, QuoteService quoteService) {
        this.policyService = policyService;
        this.emailService = emailService;
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
            @ApiParam(value = "The json of the quote to create the policy from. This quote will go through maximum " +
                    "validations")
            @RequestParam String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
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
        Policy policy;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (RuntimeException e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy.getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy payment", notes = "Updates a specific payment of a Policy. " +
            "Fields 'payment effective date' and 'payment status' will be calculated according to the given status " +
            "and the amount compare to the amount expected.", response = Payment.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class),
            @ApiResponse(code = 406, message = "If the payment id is not found in the policy payment list", response = Error.class)
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
            @RequestParam String registrationKey,
            @ApiParam(value = "The status of the transaction through the channel", required = true)
            @RequestParam SuccessErrorStatus status,
            @ApiParam(value = "The Channel", required = true)
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The credit card name given by the channel (if any)", required = false)
            @RequestParam String creditCardName,
            @ApiParam(value = "The payment method given by the channel (if any)", required = false)
            @RequestParam String paymentMethod,
            @ApiParam(value = "The error message given by the channel (if any)", required = false)
            @RequestParam String errorMessage) {
        Policy policy;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (RuntimeException e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }

        if (!policy.getPayments().stream().map(Payment::getPaymentId).anyMatch(tmp -> tmp.equals(paymentId))) {
            logger.error("Unable to find the payment with ID [" + paymentId + "] in the policy with ID [" + policyId + "]");
            return new ResponseEntity<>(POLICY_DOES_NOT_CONTAIN_PAYMENT, NOT_ACCEPTABLE);
        }

        Payment payment = policyService.updatePayment(paymentId, value, currencyCode, registrationKey, status, channelType, creditCardName, paymentMethod, errorMessage);
        return new ResponseEntity<>(JsonUtil.getJson(payment), OK);
    }

    @ApiOperation(value = "Ereceipt", notes = "Get the ereceipt of a policy by return base64 image", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If the policy doesn't exist", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/ereceipt", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getPolicyEreceipt(
            @ApiParam(value = "The policy ID")
            @PathVariable String policyId) {

        Policy policy;
        byte[] bytes;
        StringBuilder im;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (Exception e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_FOUND);
        }

        try {
            bytes = policyService.createEreceipt(policy);
            im = new StringBuilder(storePath);
            im.append(File.separator + ERECEIPT_PDF_FILE_NAME);
            im.insert(im.toString().indexOf("."), "_" + policy.getPolicyId());
            String resultFileName = im.toString();
            logger.info("Name of PDF path file [" + resultFileName + "]");
            ImageUtil.imageToPDF(bytes, eReceiptPdfStorePath);
        } catch (Exception e) {
            logger.error("Unable to create e-receipt [" + policyId + "]", e);
            return new ResponseEntity<>(UNABLE_TO_CREATE_ERECEIPT, INTERNAL_SERVER_ERROR);
        }

        try {
            emailService.sendEreceiptEmail(policy,eReceiptPdfStorePath);
        }catch (Exception e){
            logger.error("Unable to send e-receipt email [" + policyId + "]", e);
            return new ResponseEntity<>(UNABLE_TO_SEND_EMAIL, INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(JsonUtil.getJson(Base64.getEncoder().encodeToString(bytes)), OK);
    }

}