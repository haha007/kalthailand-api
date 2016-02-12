package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.service.EmailService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@Api(value = "Quotes")
public class QuoteResource {
    private final static Logger logger = LoggerFactory.getLogger(QuoteResource.class);
    private final QuoteService quoteService;
    private final EmailService emailService;
    @Value("${email.smtp.server}")
    private String smtp;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject}")
    private String subject;
    @Value("${lineid}")
    private String lineURL;

    @Inject
    public QuoteResource(QuoteService quoteService, EmailService emailService) {
        this.quoteService = quoteService;
        this.emailService = emailService;
    }

    @ApiOperation(value = "Sending email for quote", notes = "Sending email for quote", response = Quote.class)
    @RequestMapping(value = "/quotes/{quoteId}/email", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity sendEmail(
            @ApiParam(value = "The quote Id")
            @PathVariable String quoteId,
            @ApiParam(value = "The content of the graph image in base 64 encoded.")
            @RequestParam String base64Image) {

        Quote quote = null;
        try {
            quote = quoteService.findByQuoteId(quoteId);
        } catch (Exception e) {
            logger.error("Unable to get a quote for quote Id [" + quoteId + "]", e);
            return new ResponseEntity<>(ErrorCode.ERROR_WHILE_GET_QUOTE_DATA, NOT_FOUND);
        }

        if (null == quote) {
            return new ResponseEntity<>(ErrorCode.QUOTE_DOSE_NOT_EXIST, NOT_FOUND);
        } else {
            try {

                //for test
                /*
                Person person = new Person();
                person.setEmail("santi.lik@krungthai-axa.co.th");
                Insured insured = new Insured();
                insured.setPerson(person);
                quote.getInsureds().add(insured);
                */

                emailService.sendEmail(quote, base64Image, smtp, emailName, subject, lineURL);
            } catch (Exception e) {
                logger.error("Unable to send email for [" + quote.getInsureds().get(0).getPerson().getEmail() + "]", e);
                return new ResponseEntity<>(ErrorCode.UNABLE_TO_SEND_EMAIL, NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>(JsonUtil.getJson("OK"), OK);
        }
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
