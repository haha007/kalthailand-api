package th.co.krungthaiaxa.ebiz.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
@Api(value = "quotes", description = "Endpoints for quotes management")
public class QuoteResource {
    private final QuoteService quoteService;

    @Inject
    public QuoteResource(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @ApiOperation(value = "Creates an empty quote", notes = "Creates an empty quote, attached to the session ID", response = Quote.class)
    @RequestMapping(value = "/quote", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity createQuote(@RequestParam String sessionId, @RequestParam ChannelType channelType) {
        Quote quote = quoteService.createQuote(sessionId, channelType);
        return new ResponseEntity<>(JsonUtil.getJson(quote), OK);
    }

    @ApiOperation(value = "Upadtes a quote", notes = "Updates a quote, attached to the session ID, with provided JSon", response = Quote.class)
    @ApiResponses({
            @ApiResponse(code = 406, message = "If either JSon is invalid or there is no quote in the given session", response = Error.class)
    })
    @RequestMapping(value = "/quote", produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public ResponseEntity updateQuote(@RequestParam String sessionId, @RequestParam ChannelType channelType, @RequestParam String jsonQuote) {
        Quote quote;
        try {
            quote = JsonUtil.mapper.readValue(jsonQuote, Quote.class);
        } catch (IOException e) {
            return new ResponseEntity<>(ErrorCode.INVALID_QUOTE_PROVIDED, NOT_ACCEPTABLE);
        }

        Quote updatedQuote;
        try {
            updatedQuote = quoteService.updateQuote(quote);
        } catch (Exception e) {
            return new ResponseEntity<>(ErrorCode.NO_QUOTE_IN_SESSION, NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(JsonUtil.getJson(updatedQuote), OK);
    }

}
