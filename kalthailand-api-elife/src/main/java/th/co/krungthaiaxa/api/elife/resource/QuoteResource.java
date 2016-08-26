package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import th.co.krungthaiaxa.api.elife.model.SessionQuoteCount;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SessionQuoteService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
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
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    @Inject
    public QuoteResource(QuoteService quoteService, SessionQuoteService sessionQuoteService, EmailService emailService) {
        this.quoteService = quoteService;
        this.sessionQuoteService = sessionQuoteService;
        this.emailService = emailService;
    }

    @ApiOperation(value = "Sending email for quote", notes = "Sending email for quote", response = Quote.class)
    @RequestMapping(value = "/quotes/{quoteId}/email", produces = APPLICATION_JSON_VALUE, method = POST)
    @ApiResponses({
            @ApiResponse(code = 404, message = "If quote Id is unknown", response = Error.class),
            @ApiResponse(code = 500, message = "If email could not be sent", response = Error.class)
    })
    @ResponseBody
    public ResponseEntity<byte[]> sendEmail(
            @ApiParam(value = "The quote Id", required = true)
            @PathVariable String quoteId,
            @ApiParam(value = "The content of the graph image in base 64 encoded.", required = false)
            @RequestBody(required = false) String base64Image,
            @ApiParam(value = "The session id the quote is in", required = true)
            @RequestParam String sessionId,
            @ApiParam(value = "The channel being used to create the quote.", required = true)
            @RequestParam ChannelType channelType) {
        Optional<Quote> quote = quoteService.findByQuoteId(quoteId, sessionId, channelType);
        if (!quote.isPresent()) {
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED), NOT_FOUND);
        }

        try {
            if (quote.get().getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
                emailService.sendQuote10ECEmail(quote.get(), base64Image);
            } else if (quote.get().getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
                emailService.sendQuoteiFineEmail(quote.get());
            }
        } catch (Exception e) {
            logger.error("Unable to send email for [" + quote.get().getInsureds().get(0).getPerson().getEmail() + "]", e);
            return new ResponseEntity<>(getJson(ErrorCode.UNABLE_TO_SEND_EMAIL.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(getJson(""), OK);
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
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED), NOT_FOUND);
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
            return new ResponseEntity<>(getJson(ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED), NOT_FOUND);
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

    @ApiOperation(value = "Count sessionQuotes for every product", notes = "Count sessionQuotes for every product", response = SessionQuoteCount.class, responseContainer = "List")
    @RequestMapping(value = "/quotes/count/{dateFrom}/{dateTo}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public List<SessionQuoteCount> getTotalQuoteCount(
            @ApiParam(value = "The date from", required = true)
            @PathVariable String dateFrom,
            @ApiParam(value = "The date to", required = true)
            @PathVariable String dateTo) {

        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(dateFrom)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateFrom));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(dateTo)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateTo));
        }

        return sessionQuoteService.countSessionQuotesOfAllProducts(startDate, endDate);
    }

    @ApiOperation(value = "Download total number of quote excel file", notes = "Download total number of quote excel file", response = Quote.class, responseContainer = "List")
    @RequestMapping(value = "/quotes/count/download", method = GET)
    @ResponseBody
    public void downloadTotalQuoteCountExcelFile(
            @ApiParam(value = "The date from")
            @RequestParam(required = true) String dateFrom,
            @ApiParam(value = "The date to")
            @RequestParam(required = true) String dateTo,
            HttpServletResponse response) {

        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(dateFrom)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateFrom));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(dateTo)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateTo));
        }

        String now = getDateTimeNow();

        byte[] content = sessionQuoteService.exportTotalQuotesCountReport(startDate, endDate, now);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(content.length);

        String fileName = "eLife_TotalQuoteCountExtract_" + now + ".xlsx";
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the quote total count excel file", e);
        }

    }

    private String getDateTimeNow() {
        return ofPattern("yyyyMMdd_HHmmss").format(now());
    }

    /*
     @ApiOperation(value = "Policies extract", notes = "Gets the policy extract for commission calculation. Result is an Excel file", response = Policy.class, responseContainer = "List")
    @RequestMapping(value = "/policies/extract/download", method = GET)
    @ResponseBody
    public void getPoliciesExcelFile(
            @ApiParam(value = "Part of policy Id to filter with")
            @RequestParam(required = false) String policyId,
            @ApiParam(value = "The product type to filter with")
            @RequestParam(required = false) ProductType productType,
            @ApiParam(value = "The policy status to filter with")
            @RequestParam(required = false) PolicyStatus status,
            @ApiParam(value = "True to return only Policies with previous agent code, false to return Policies with empty agent codes, empty to return all Policies")
            @RequestParam(required = false) Boolean nonEmptyAgentCode,
            @ApiParam(value = "To filter Policies starting after the given date")
            @RequestParam(required = false) String fromDate,
            @ApiParam(value = "To filter Policies ending before the given date")
            @RequestParam(required = false) String toDate,
            HttpServletResponse response) {
        LocalDate startDate = null;
        if (StringUtils.isNoneEmpty(fromDate)) {
            startDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(fromDate));
        }

        LocalDate endDate = null;
        if (StringUtils.isNoneEmpty(toDate)) {
            endDate = LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(toDate));
        }

        List<Policy> policies = policyService.findAll(policyId, productType, status, nonEmptyAgentCode, startDate, endDate);

        String now = ofPattern("yyyyMMdd_HHmmss").format(now());
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PolicyExtract_" + now);

        ExcelUtils.appendRow(sheet,
                text("Policy ID"),
                text("Previous Policy ID"),
                text("Agent Code 1"),
                text("Agent Code 2"));
        policies.stream().forEach(tmp -> createPolicyExtractExcelFileLine(sheet, tmp));
        ExcelUtils.autoWidthAllColumns(workbook);

        byte[] content;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(content.length);

        String fileName = "eLife_PolicyExtract_" + now + ".xlsx";
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(content, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the deduction file", e);
        }
    }
     */

}
