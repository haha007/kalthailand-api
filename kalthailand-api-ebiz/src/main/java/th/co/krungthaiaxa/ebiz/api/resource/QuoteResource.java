package th.co.krungthaiaxa.ebiz.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.model.error.ErrorCode;
import th.co.krungthaiaxa.ebiz.api.service.QuoteService;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@Api(value = "Quotes")
public class QuoteResource {
    private final static Logger logger = LoggerFactory.getLogger(QuoteResource.class);
    private final QuoteService quoteService;

    @Inject
    public QuoteResource(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @ApiOperation(value = "Creates an empty quote", notes = "Creates an empty quote, attached to the session ID. " +
            "If there is already a quote for the session ID, then the quote returned will be the one saved in the " +
            "database in its latest state.", response = Quote.class)
    @RequestMapping(value = "/quotes", produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity createQuote(
            @ApiParam(value = "The session id. Must be unique through the Channel. This is used to recover unfinished quotes throught the channel")
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.")
            @RequestParam ChannelType channelType) {
        Quote quote = quoteService.createQuote(sessionId, channelType);
        return new ResponseEntity<>(JsonUtil.getJson(quote), OK);
    }

    @ApiOperation(value = "Updates a quote", notes = "Updates a quote with provided JSon. Calculatiom may occur if " +
            "enough elements are provided.", response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session",
                    response = Error.class)
    })
    @RequestMapping(value = "/quotes", produces = APPLICATION_JSON_VALUE, method = PUT)
    public ResponseEntity updateQuote(
            @ApiParam(value = "The json of the quote. This quote will be updated with given values and will go through minimal validations")
            @RequestParam String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            logger.error("Unable to get a quote out of [" + jsonQuote + "]", e);
            return new ResponseEntity<>(ErrorCode.INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }

        Quote updatedQuote;
        try {
            updatedQuote = quoteService.updateQuote(quote);
        } catch (Exception e) {
            logger.error("Unable to update quote", e);
            return new ResponseEntity<>(ErrorCode.NO_QUOTE_IN_SESSION, NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(JsonUtil.getJson(updatedQuote), OK);
    }

}
