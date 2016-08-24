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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteCriteriaRepositoryTest {
    Logger logger = LoggerFactory.getLogger(QuoteCriteriaRepositoryTest.class);
    @Inject
    private QuoteCriteriaRepository quoteCriteriaRepository;

    @Inject
    private QuoteRepository quoteRepository;

    @Test
    public void should_return_quote_count_greather_than_zero() {
        LocalDate today = LocalDate.now();
        LocalDate beforeDay = today.minusDays(30);
        List<Map<String, Object>> listCount = quoteCriteriaRepository.quoteCount(beforeDay, today);
        //TODO This UnitTest will fail with empty data. Should not checking this criteria!
        assertThat(listCount.size()).isGreaterThan(0);
    }

    @Test
    public void should_return_quote_count_zero() {
        LocalDate today = LocalDate.now();
        LocalDate beforeDay = today.minusDays(30);
        List<Map<String, Object>> listCount = quoteCriteriaRepository.quoteCount(today, beforeDay);
        assertThat(listCount.size()).isEqualTo(0);
    }

    @Test
    public void performance_test() {
        LocalDate today = LocalDate.now();
        LocalDate beforeDay = today.minusMonths(3);
        Instant startTime = Instant.now();
        int totalCount = 0;
        for (ProductType productType : ProductType.values()) {
            Instant startTimeForProduct = Instant.now();
            long count = quoteRepository.countByProductIdAndStartDateInRange(productType.getName(), beforeDay.atStartOfDay(), today.atStartOfDay());
//            long count = quoteRepository.countByProductIdAndInsuredStartDateInRange(productType.getName(), beforeDay, today);
            totalCount += count;
            logRuntime(startTimeForProduct, String.format("\tCount [%s]: %s", productType.getName(), count));
        }
        logRuntime(startTime, String.format("Count for way 1: %s", totalCount));

//        List<Map<String, Object>> listCount = quoteCriteriaRepository.quoteCount(beforeDay, today);
//        for (Map<String, Object> stringObjectMap : listCount) {
//            logger.debug("Count for way 2: {}", stringObjectMap);
//        }
//        logRuntime(startTime, String.format("Count way 2"));
//        assertThat(listCount.size()).isGreaterThan(0);
//        TODO This UnitTest will fail with empty data. Should not checking this criteria!
    }

    private Instant logRuntime(Instant startTime, String msg) {
        Instant now = Instant.now();
        long runTimeMilli = now.toEpochMilli() - startTime.toEpochMilli();
        long runTimeSeconds = runTimeMilli / 1000;
        logger.debug(String.format("%s\nRuntime: %s ms ~ %s s", msg, runTimeMilli, runTimeSeconds));
        return now;
    }
}
