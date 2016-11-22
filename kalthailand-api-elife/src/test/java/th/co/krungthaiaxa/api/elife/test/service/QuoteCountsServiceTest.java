package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.QuoteCount;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.QuoteCountForAllProductsService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author khoi.tran on 10/6/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteCountsServiceTest extends ELifeTest {
    @Autowired
    QuoteFactory quoteFactory;

    @Autowired
    QuoteService quoteService;
    @Autowired
    QuoteCountForAllProductsService quoteCountForAllProductsService;

    @Test
    public void test_quote_count_correct_number() {
        //The method should be able to search from beginning of fromDate to ending of toDate. So we can test with fromDateTime & endDateTime at the same date.
        LocalDateTime fromDateTime = LocalDateTime.now();
        LocalDateTime toDateTime = LocalDateTime.now();

        List<QuoteCount> quoteCountListBefore = quoteCountForAllProductsService.countQuotesOfAllProducts(fromDateTime, toDateTime);

        //Create one new iProtect
        quoteFactory.createDefaultIProtect();

        //Create new iGen
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIGen();
        //Create another iGen with the same quoteSessionId
        quoteFactory.createQuote(quoteResult.getSessionId(), ProductQuotationFactory.constructIGenDefault(), TestUtil.TESTING_EMAIL);

        toDateTime = LocalDateTime.now();
        List<QuoteCount> quoteCountListAfter = quoteCountForAllProductsService.countQuotesOfAllProducts(fromDateTime, toDateTime);

        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IFINE, 0, 0);
        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IPROTECT, -1, -1);
        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IGEN, -1, -2);
    }

    private void assertQuoteCountsDiff(List<QuoteCount> quoteCountsBefore, List<QuoteCount> quoteCountsAfter, ProductType productType, long diffSessionQuotes, long diffQuotes) {
        QuoteCount quoteCountBefore = findQuoteCount(quoteCountsBefore, productType);
        QuoteCount quoteCountAfter = findQuoteCount(quoteCountsAfter, productType);
        assertQuoteCountDiff(quoteCountBefore, quoteCountAfter, diffSessionQuotes, diffQuotes);
    }

    private QuoteCount findQuoteCount(List<QuoteCount> quoteCounts, ProductType productType) {
        for (QuoteCount quoteCount : quoteCounts) {
            if (quoteCount.getProductId().equals(productType.getLogicName())) {
                return quoteCount;
            }
        }
        return null;
    }

    private void assertQuoteCountDiff(QuoteCount before, QuoteCount after, long diffSessionQuotes, long diffNumber) {
        Assert.assertEquals((long) before.getSessionQuoteCount(), after.getSessionQuoteCount() + diffSessionQuotes);
        Assert.assertEquals((long) before.getQuoteCount(), after.getQuoteCount() + diffNumber);
    }
}
