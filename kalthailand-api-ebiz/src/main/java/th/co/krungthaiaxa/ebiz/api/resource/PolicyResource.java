package th.co.krungthaiaxa.ebiz.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.model.error.ErrorCode;
import th.co.krungthaiaxa.ebiz.api.service.PolicyService;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@Api(value = "policies", description = "Everything for policy")
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
    public ResponseEntity createQuote(
            @ApiParam(value = "The json of the quote to create the policy from. This quote will go through maximum validations")
            @RequestParam String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(ErrorCode.INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }

        Policy policy;
        try {
            policy = policyService.createPolicy(quote);
        } catch (PolicyValidationException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(ErrorCode.INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        } catch (QuoteCalculationException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(ErrorCode.INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(JsonUtil.getJson(policy), OK);
    }

    @ApiOperation(value = "Updates a policy", notes = "Updates a policy to save additional information like payment methods", response = Policy.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session", response = Error.class)
    })
    @RequestMapping(value = "/policies", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updateQuote(
            @ApiParam(value = "The json of the policy. This policy will be updated with given values")
            @RequestParam String jsonPolicy) {
        Policy policy;
        try {
            policy = JsonUtil.mapper.readValue(jsonPolicy, Policy.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonPolicy + "]", e);
            return new ResponseEntity<>(ErrorCode.INVALID_POLICY_PROVIDED, NOT_ACCEPTABLE);
        }

        Policy updatedPolicy = policyService.update(policy);
        return new ResponseEntity<>(JsonUtil.getJson(updatedPolicy), OK);
    }

}
