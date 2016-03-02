package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.products.Product;
import th.co.krungthaiaxa.elife.api.products.ProductQuotation;
import th.co.krungthaiaxa.elife.api.service.EmailService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.elife.api.products.ProductFactory.getProduct;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@RestController
@Api(value = "Quotes")
public class QuoteResource {
    private final static Logger logger = LoggerFactory.getLogger(QuoteResource.class);
    private final QuoteService quoteService;
    private final EmailService emailService;

    @Inject
    public QuoteResource(QuoteService quoteService, EmailService emailService) {
        this.quoteService = quoteService;
        this.emailService = emailService;
    }

    @ApiOperation(value = "Sending email for quote", notes = "Sending email for quote", response = Quote.class)
    @RequestMapping(value = "/quotes/{quoteId}/email", produces = APPLICATION_JSON_VALUE, method = POST)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown", response = Error.class),
            @ApiResponse(code = 500, message = "If email could not be sent", response = Error.class)
    })
    @ResponseBody
    public ResponseEntity sendEmail(
            @ApiParam(value = "The quote Id")
            @PathVariable String quoteId,
            @ApiParam(value = "The content of the graph image in base 64 encoded.")
            @RequestBody String base64Image,
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!quote.isPresent()) {
            return new ResponseEntity<>(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED, NOT_FOUND);
        }

        try {
            emailService.sendQuoteEmail(quote.get(), base64Image);
        } catch (Exception e) {
            logger.error("Unable to send email for [" + quote.get().getInsureds().get(0).getPerson().getEmail() + "]", e);
            return new ResponseEntity<>(UNABLE_TO_SEND_EMAIL.apply(e.getMessage()), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(getJson(""), OK);
    }

    @ApiOperation(value = "Get latest Quote", notes = "Returns the latest quote attached to the given sessionId. " +
            "Returns empty result if nothing found.", response = Quote.class)
    @RequestMapping(value = "/quotes/latest", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getLatestQuote(
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = quoteService.getLatestQuote(sessionId, channelType);
        if (!quote.isPresent()) {
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
    public ResponseEntity getQuote(
            @ApiParam(value = "The quote Id")
            @PathVariable String quoteId,
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!quote.isPresent()) {
            return new ResponseEntity<>(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED, NOT_FOUND);
        }

        return new ResponseEntity<>(getJson(quote.get()), OK);
    }

    @ApiOperation(value = "Creates an empty quote", notes = "Creates an empty quote, attached to the session ID.",
            response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If product Id is unknown", response = Error.class)
    })
    @RequestMapping(value = "/quotes", produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity createQuote(
            @ApiParam(value = "The session id. Must be unique through the Channel.")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The product details for which to create a quote on")
            @RequestBody ProductQuotation productQuotation) {
        try {
            return new ResponseEntity<>(getJson(quoteService.createQuote(sessionId, channelType, productQuotation)), OK);
        } catch (QuoteCalculationException e) {
            logger.error("Unable to update quote", e);
            return new ResponseEntity<>(QUOTE_NOT_CREATED, NOT_ACCEPTABLE);
        }
    }

    @ApiOperation(value = "Updates a quote", notes = "Updates a quote with is provided JSon. Calculation may occur " +
            "if enough elements are provided.", response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown or if sessionId user does not have access to the quote", response = Error.class),
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session",
                    response = Error.class)
    })
    @RequestMapping(value = "/quotes/{quoteId}", produces = APPLICATION_JSON_VALUE, method = PUT)
    public ResponseEntity updateQuote(
            @ApiParam(value = "The quote Id")
            @PathVariable String quoteId,
            @ApiParam(value = "The session id the quote is in")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType,
            @ApiParam(value = "The json of the quote. This quote will be updated with given values and will go through minimal validations")
            @RequestBody String jsonQuote) {
        Optional<Quote> tmp = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!tmp.isPresent()) {
            return new ResponseEntity<>(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED, NOT_FOUND);
        }

        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }
        quote.setId(tmp.get().getId());
        quote.setQuoteId(tmp.get().getQuoteId());

        Quote updatedQuote;
        try {
            updatedQuote = quoteService.updateQuote(quote);
        } catch (QuoteCalculationException e) {
            logger.error("Unable to update quote", e);
            return new ResponseEntity<>(QUOTE_NOT_UPDATED, NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(getJson(updatedQuote), OK);
    }

}
