package th.co.krungthaiaxa.api.elife.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.QuoteCount;
import th.co.krungthaiaxa.api.elife.products.ProductType;

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
    QuoteCountForAllProductsService quoteCountForAllProductsService;

    @Test
    public void test_quote_count_correct_number() {
        LocalDateTime fromDateTime = LocalDateTime.now();
        LocalDateTime toDateTime = LocalDateTime.now();

        List<QuoteCount> quoteCountListBefore = quoteCountForAllProductsService.countQuotesOfAllProducts(fromDateTime, toDateTime);
        quoteFactory.createDefaultIGen();
        quoteFactory.createDefaultIProtect();

        toDateTime = LocalDateTime.now();
        List<QuoteCount> quoteCountListAfter = quoteCountForAllProductsService.countQuotesOfAllProducts(fromDateTime, toDateTime);

        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IFINE, 0);
        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IGEN, -1);
        assertQuoteCountsDiff(quoteCountListBefore, quoteCountListAfter, ProductType.PRODUCT_IPROTECT, -1);
    }

    private void assertQuoteCountsDiff(List<QuoteCount> quoteCountsBefore, List<QuoteCount> quoteCountsAfter, ProductType productType, long diffNumber) {
        QuoteCount quoteCountBefore = findQuoteCount(quoteCountsBefore, productType);
        QuoteCount quoteCountAfter = findQuoteCount(quoteCountsAfter, productType);
        assertQuoteCountDiff(quoteCountBefore, quoteCountAfter, diffNumber);
    }

    private QuoteCount findQuoteCount(List<QuoteCount> quoteCounts, ProductType productType) {
        for (QuoteCount quoteCount : quoteCounts) {
            if (quoteCount.getProductId().equals(productType.getLogicName())) {
                return quoteCount;
            }
        }
        return null;
    }

    private void assertQuoteCountDiff(QuoteCount before, QuoteCount after, long diffNumber) {
        Assert.assertEquals((long) before.getQuoteCount(), after.getQuoteCount() + diffNumber);
        Assert.assertEquals((long) before.getSessionQuoteCount(), after.getSessionQuoteCount() + diffNumber);
    }
}
