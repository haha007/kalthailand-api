package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductEmailService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SessionQuoteService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;

@RestController
@Api(value = "Quotes")
public class QuoteResource {
    private final static Logger logger = LoggerFactory.getLogger(QuoteResource.class);
    private final QuoteService quoteService;
    private final SessionQuoteService sessionQuoteService;
    private final EmailService emailService;

    private final ProductEmailService productEmailService;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    @Inject
    public QuoteResource(QuoteService quoteService, SessionQuoteService sessionQuoteService, EmailService emailService, ProductEmailService productEmailService) {
        this.quoteService = quoteService;
        this.sessionQuoteService = sessionQuoteService;
        this.emailService = emailService;
        this.productEmailService = productEmailService;
    }

    @ApiOperation(value = "Sending email for quote", notes = "Sending email for quote", response = Quote.class)
    @RequestMapping(value = "/quotes/{quoteId}/email", produces = APPLICATION_JSON_VALUE, method = POST)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown", response = Error.class),
            @ApiResponse(code = 500, message = "If email could not be sent", response = Error.class)
    })
    @ResponseBody
    public void sendEmail(
            @ApiParam(value = "The quote Id", required = true)
            @PathVariable String quoteId,
            @ApiParam(value = "The content of the graph image in base 64 encoded.", required = false)
            @RequestBody(required = false) String base64Image,
            @ApiParam(value = "The session id the quote is in", required = true)
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.", required = true)
            @RequestParam ChannelType channelType) {
        productEmailService.sendQuoteEmail(quoteId, sessionId, channelType, base64Image);
    }

    @ApiOperation(value = "Get latest Quote", notes = "Returns the latest quote attached to the given sessionId. " +
            "Returns empty result if nothing found.", response = Quote.class)
    @RequestMapping(value = "/quotes/latest", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getLatestQuote(
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = null;
        try {
            quote = quoteService.getLatestQuote(sessionId, channelType);
        } catch (DataAccessException e) {
            logger.error("Unable to get latest quote", e);
        }
        if (quote == null || !quote.isPresent()) {
            return new ResponseEntity<>(getJson(""), OK);
        } else {
            return new ResponseEntity<>(getJson(quote.get()), OK);
        }
    }

    @ApiOperation(value = "Detail of a quote", notes = "Gets the details of a quote given its ID", response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown or if sessionId user does not have access to the quote", response = Error.class)
    })
    @RequestMapping(value = "/quotes/{quoteId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getQuote(
            @ApiParam(value = "The quote Id", required = true)
            @PathVariable String quoteId,
            @ApiParam(value = "The session id the quote is in", required = true)
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.", required = true)
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!quote.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST), NOT_FOUND);
        }

        return new ResponseEntity<>(getJson(quote.get()), OK);
    }

    @ApiOperation(value = "Create a quote", notes = "Creates a quote, attached to the session ID, with product " +
            "details and calculated fields.",
            response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 500, message = "If quote has not been created", response = Error.class)
    })
    @RequestMapping(value = "/quotes", produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity<byte[]> createQuote(
            @ApiParam(value = "The session id. Must be unique through the Channel.")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The product details for which to create a quote on")
            @RequestBody ProductQuotation productQuotation) {
        try {
            return new ResponseEntity<>(getJson(quoteService.createQuote(sessionId, channelType, productQuotation)), OK);
        } catch (ElifeException e) {
            logger.error("Unable to create a quote", e);
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_NOT_CREATED), INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Updates a quote", notes = "Updates a quote with is provided JSon. Calculation may occur " +
            "if enough elements are provided.", response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown or if sessionId user does not have access to the quote", response = Error.class),
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session",
                    response = Error.class),
            @ApiResponse(code = 500, message = "If quote has not been updated", response = Error.class)
    })
    @RequestMapping(value = "/quotes/{quoteId}", produces = APPLICATION_JSON_VALUE, method = PUT)
    public ResponseEntity<byte[]> updateQuote(
            @ApiParam(value = "The quote Id", required = true)
            @PathVariable String quoteId,
            @ApiParam(value = "The session id the quote is in", required = true)
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.", required = true)
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The json of the quote. This quote will be updated with given values and will go through minimal validations", required = true)
            @RequestBody String jsonQuote,
            HttpServletRequest httpServletRequest) {
        Optional<Quote> tmp = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST), NOT_FOUND);
        }

        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_QUOTE_PROVIDED), NOT_ACCEPTABLE);
        }
        quote.setId(tmp.get().getId());
        quote.setQuoteId(tmp.get().getQuoteId());

        Quote updatedQuote;
        try {
            updatedQuote = quoteService.updateQuote(quote, httpServletRequest.getHeader(tokenHeader));
        } catch (ElifeException e) {
            logger.error("Unable to update quote", e);
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_NOT_UPDATED.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(getJson(updatedQuote), OK);
    }
}
