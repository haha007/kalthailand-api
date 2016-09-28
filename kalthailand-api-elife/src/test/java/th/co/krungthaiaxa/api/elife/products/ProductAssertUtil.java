package th.co.krungthaiaxa.api.elife.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.model.CommonData;

/**
 * @author khoi.tran on 9/28/16.
 */
public class ProductAssertUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductAssertUtil.class);

    public static void assertProductAmountsWithFullDetail(ProductAmounts productAmounts) {
        LOGGER.debug("\n" + ObjectMapperUtil.toJson(new ObjectMapper(), productAmounts));

        Assert.assertNotNull(productAmounts.getMaxSumInsured());
        Assert.assertNotNull(productAmounts.getMinPremium());
        Assert.assertNotNull(productAmounts.getMinSumInsured());
        Assert.assertNotNull(productAmounts.getMaxPremium());

        CommonData commonData = productAmounts.getCommonData();
        Assert.assertNotNull(commonData.getProductId());

        Assert.assertNotNull(commonData.getMaxSumInsured());
        Assert.assertNotNull(commonData.getMinPremium());
        Assert.assertNotNull(commonData.getMinSumInsured());
        Assert.assertNotNull(commonData.getMaxPremium());
    }

    public static void assertProductAmountsWithSumInsureLimits(ProductAmounts productAmounts) {
        LOGGER.debug("\n" + ObjectMapperUtil.toJson(new ObjectMapper(), productAmounts));

        Assert.assertNotNull(productAmounts.getMaxSumInsured());
        Assert.assertNotNull(productAmounts.getMinSumInsured());

        CommonData commonData = productAmounts.getCommonData();
        Assert.assertNotNull(commonData.getProductId());

        Assert.assertNotNull(commonData.getMaxSumInsured());
        Assert.assertNotNull(commonData.getMinSumInsured());
    }

    public static void assertCommonDataAfterQuoteCalculationWithFullDetail(CommonData commonData) {
        Assert.assertNotNull(commonData.getProductId());

        Assert.assertNotNull(commonData.getMaxSumInsured());
        Assert.assertNotNull(commonData.getMinPremium());
        Assert.assertNotNull(commonData.getMinSumInsured());
        Assert.assertNotNull(commonData.getMaxPremium());

        Assert.assertNotNull(commonData.getNbOfYearsOfCoverage());
        Assert.assertNotNull(commonData.getNbOfYearsOfPremium());

        Assert.assertNotNull(commonData.getMinAge());
        Assert.assertNotNull(commonData.getMaxAge());

        Assert.assertNotNull(commonData.getProductCurrency());
    }
}
