package th.co.krungthaiaxa.api.elife.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SessionQuoteRepositoryTest {
    Logger logger = LoggerFactory.getLogger(SessionQuoteRepositoryTest.class);

    @Inject
    private SessionQuoteRepository sessionQuoteRepository;

    @Test
    public void should_count_session_quotes_successfully() {
        LocalDate today = LocalDate.now();
        LocalDate beforeDay = today.minusMonths(3);
        Instant startTime = Instant.now();
        int totalCount = 0;
        for (ProductType productType : ProductType.values()) {
            Instant startTimeForProduct = Instant.now();
            long countForEachProduct = sessionQuoteRepository.countByProductIdAndStartDateInRange(productType.getName(), beforeDay.atStartOfDay(), today.atStartOfDay());
            totalCount += countForEachProduct;
            logRuntime(startTimeForProduct, String.format("\tCount [%s]: %s", productType.getName(), countForEachProduct));
        }
        logRuntime(startTime, String.format("Count for way 2: %s", totalCount));
    }

    private Instant logRuntime(Instant startTime, String msg) {
        Instant now = Instant.now();
        long runTimeMilli = now.toEpochMilli() - startTime.toEpochMilli();
        long runTimeSeconds = runTimeMilli / 1000;
        logger.debug(String.format("%s \t Runtime: %s ms ~ %s s", msg, runTimeMilli, runTimeSeconds));
        return now;
    }
}
