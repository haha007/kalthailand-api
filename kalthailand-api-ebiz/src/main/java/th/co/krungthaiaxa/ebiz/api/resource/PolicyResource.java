package th.co.krungthaiaxa.ebiz.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.ebiz.api.model.Payment;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.service.PolicyService;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.ebiz.api.model.error.ErrorCode.*;

@RestController
@Api(value = "Policies")
public class PolicyResource {
    private final static Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private final PolicyService policyService;

    @Inject
    public PolicyResource(PolicyService policyService) {
        this.policyService = policyService;
    }

    @ApiOperation(value = "Creates a policy", notes = "Creates a policy out of a quote. Policy will be created only if it went through all product validation.", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If JSon of quote is invalid or if Policy could not be created", response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity createPolicy(
            @ApiParam(value = "The json of the quote to create the policy from. This quote will go through maximum validations")
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

    @ApiOperation(value = "Policy payments", notes = "Get the payments of a policy", response = Payment.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getPolicyPayments(
            @ApiParam(value = "The policy ID")
            @RequestParam String policyId) {
        Policy policy;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (Exception e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy.getPayments()), OK);
    }

    @ApiOperation(value = "Update Policy payment", notes = "Updates a specific payment of a Policy", response = Payment.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session", response = Error.class)
    })
    @RequestMapping(value = "/policies/{policyId}/payments/{paymentId}", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updatePolicyPayment(
            @ApiParam(value = "The policy ID")
            @RequestParam String policyId) {
        Policy policy;
        try {
            policy = policyService.findPolicy(policyId);
        } catch (Exception e) {
            logger.error("Unable to find the policy with ID [" + policyId + "]", e);
            return new ResponseEntity<>(POLICY_DOES_NOT_EXIST, NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy.getPayments()), OK);
    }

}
