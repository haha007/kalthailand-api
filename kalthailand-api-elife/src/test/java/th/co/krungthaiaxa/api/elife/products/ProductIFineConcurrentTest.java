package th.co.krungthaiaxa.api.elife.products;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductIFineConcurrentTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductIFineConcurrentTest.class);
    @Inject
    private QuoteService quoteService;

    private List<ProductQuotation> initProductQuotations() {
        List<ProductQuotation> result = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
//            result.add(productQuotation(IFINE1, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
//            result.add(productQuotation(IFINE2, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
//            result.add(productQuotation(IFINE3, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
//            result.add(productQuotation(IFINE4, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
//            result.add(productQuotation(IFINE5, 20, EVERY_YEAR, GenderCode.MALE, FALSE));
        }
        return result;
    }

    @Test
    public void should_run_concurrently() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            List<ProductQuotation> productQuotations = initProductQuotations();
            for (ProductQuotation productQuotation : productQuotations) {
                executor.execute(new CalculateQuoteTask(quoteService, productQuotation));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    private static class CalculateQuoteTask implements Runnable {
        private static int taskId = 0;
        private final QuoteService quoteService;
        private final ProductQuotation productQuotation;

        private CalculateQuoteTask(QuoteService quoteService, ProductQuotation productQuotation) {
            this.quoteService = quoteService;
            this.productQuotation = productQuotation;
        }

        @Override
        public void run() {
            int taskNumber = taskId++;

            String packageNameInRequest = productQuotation.getPackageName();
            ProductIFinePackage productIFinePackageInRequest = ProductIFinePackage.valueOf(packageNameInRequest);
            LOGGER.info("[{}]\tRequest:\t {}", taskNumber, productIFinePackageInRequest);
            Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);

            ProductIFinePackage productIFinePackageInResult = quote.getPremiumsData().getProductIFinePremium().getProductIFinePackage();
            LOGGER.info("[{}]\tResult:\t {}", taskNumber, productIFinePackageInResult);
            Assert.assertEquals(productIFinePackageInRequest, productIFinePackageInResult);
        }
    }
}
