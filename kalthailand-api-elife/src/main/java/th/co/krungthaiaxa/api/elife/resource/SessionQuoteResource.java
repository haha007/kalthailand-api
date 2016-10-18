package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.SessionQuoteCount;
import th.co.krungthaiaxa.api.elife.service.SessionQuoteService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "SessionQuotes")
public class SessionQuoteResource {
    private final static Logger logger = LoggerFactory.getLogger(SessionQuoteResource.class);
    private final SessionQuoteService sessionQuoteService;

    @Inject
    public SessionQuoteResource(SessionQuoteService sessionQuoteService) {
        this.sessionQuoteService = sessionQuoteService;
    }

    @ApiOperation(value = "Count sessionQuotes for every product", notes = "Count sessionQuotes for every product", response = SessionQuoteCount.class, responseContainer = "List")
    @RequestMapping(value = "/session-quotes/all-products-counts", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public List<SessionQuoteCount> getTotalQuoteCount(
            @ApiParam(value = "The start searching date", required = true)
            @RequestParam("fromDate") String startDateString,
            @ApiParam(value = "The end searching date", required = true)
            @RequestParam("toDate") String endDateString) {
        LocalDateTime startDate = DateTimeUtil.toLocalDateTimePatternISO(startDateString);
        LocalDateTime endDate = DateTimeUtil.toLocalDateTimePatternISO(endDateString);
        return sessionQuoteService.countSessionQuotesOfAllProducts(startDate, endDate);
    }

    @ApiOperation(value = "Excel report for counting session quotes", notes = "Export Excel report for counting sessionQuotes for every product", response = Quote.class, responseContainer = "List")
    @RequestMapping(value = "/session-quotes/all-products-counts/download", method = GET)
    @ResponseBody
    public void downloadTotalQuoteCountExcelFile(
            @ApiParam(value = "The start searching date", required = true)
            @RequestParam("fromDate") String startDateString,
            @ApiParam(value = "The end searching date", required = true)
            @RequestParam("toDate") String endDateString,
            HttpServletResponse response) {

        LocalDateTime startDate = DateTimeUtil.toLocalDateTimePatternISO(startDateString);
        LocalDateTime endDate = DateTimeUtil.toLocalDateTimePatternISO(endDateString);
        byte[] content = sessionQuoteService.exportTotalQuotesCountReport(startDate, endDate);
        DownloadUtil.writeBytesToResponseWithDateRollFileName(response, content, "session-quotes-count");
    }


}
