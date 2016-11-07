package th.co.krungthaiaxa.api.elife.test.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.SessionQuoteRepository;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
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
            long countForEachProduct = sessionQuoteRepository.countByProductIdAndStartDateInRange(productType.getLogicName(), beforeDay.atStartOfDay(), today.atStartOfDay());
            totalCount += countForEachProduct;
            LogUtil.logRuntime(startTimeForProduct, String.format("\t[%s]: %s", productType.getLogicName(), countForEachProduct));
        }
        LogUtil.logRuntime(startTime, String.format("Total session quotes: %s", totalCount));
    }

}
