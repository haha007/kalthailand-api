package th.co.krungthaiaxa.api.elife.test.repository;

import org.castor.core.util.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.QuoteMid;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepositoryExtends;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author tuong.le on 3/17/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class QuoteRepositoryExtendsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteRepositoryExtendsTest.class);

    @Inject
    private QuoteRepositoryExtends quoteRepositoryExtends;

    @Test
    public void should_get_quote_mid_list_of_all_product_type_in_3_months_ago() {
        final LocalDateTime now = DateTimeUtil.nowLocalDateTimeInThaiZoneId();
        final List<ProductType> allProductType = Arrays.asList(ProductType.values());
        //get quote mid of all product type in 3 months ago
        final List<QuoteMid> result = quoteRepositoryExtends.
                getDistinctQuoteMid(allProductType,
                        now.minusMonths(3),
                        now);
        Assert.notNull(result, "Could not get Quote MID list by all product type in 3 months from now {}");
        LOGGER.info("Found {} Quote MID of all Product in 3 months ago", result.size());
    }

    @Test
    public void should_get_quote_mid_list_of_igen_in_3_months_ago() {
        final LocalDateTime now = DateTimeUtil.nowLocalDateTimeInThaiZoneId();
        final List<ProductType> igenProductType = Collections.singletonList(ProductType.PRODUCT_IGEN);
        //get quote mid of iGen in 3 months ago
        final List<QuoteMid> result = quoteRepositoryExtends.
                getDistinctQuoteMid(igenProductType,
                        now.minusMonths(3),
                        now);
        Assert.notNull(result, "Could not get Quote MID list by iGen in 3 months from now {}");
        LOGGER.info("Found {} Quote MID of iGen in 3 months ago", result.size());
    }
}
