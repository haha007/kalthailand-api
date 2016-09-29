package th.co.krungthaiaxa.api.elife.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.DateTimeAmount;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.PremiumsData;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * @author khoi.tran on 9/28/16.
 */
public class ProductAssertUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductAssertUtil.class);
    public static final double DOUBLE_COMPARE_EXACT_VALUE = 0.01;

    public static void assertProductAmountsWithFullDetail(ProductAmounts productAmounts) {
        LOGGER.debug("\nProductAmounts: " + ObjectMapperUtil.toJson(new ObjectMapper(), productAmounts));

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
        LOGGER.debug("\nProductAmounts: " + ObjectMapperUtil.toJson(new ObjectMapper(), productAmounts));

        Assert.assertNotNull(productAmounts.getMaxSumInsured());
        Assert.assertNotNull(productAmounts.getMinSumInsured());

        CommonData commonData = productAmounts.getCommonData();
        Assert.assertNotNull(commonData.getProductId());

        Assert.assertNotNull(commonData.getMaxSumInsured());
        Assert.assertNotNull(commonData.getMinSumInsured());
    }

    public static void assertAmountLimits(ProductAmounts productAmounts, Double expectedMinSumInsured, Double expectedMaxSumInsured, Double expectedMinPremium, Double expectedMaxPremium) {
        LOGGER.debug("\nProductAmounts: " + ObjectMapperUtil.toJson(new ObjectMapper(), productAmounts));
        if (expectedMinSumInsured != null) {
            Assert.assertEquals(expectedMinSumInsured, productAmounts.getMinSumInsured());
        }
        if (expectedMaxSumInsured != null) {
            Assert.assertEquals(expectedMaxSumInsured, productAmounts.getMaxSumInsured());
        }
        if (expectedMinPremium != null) {
            Assert.assertEquals(expectedMinPremium, productAmounts.getMinPremium());
        }
        if (expectedMaxPremium != null) {
            Assert.assertEquals(expectedMaxPremium, productAmounts.getMaxPremium());
        }
    }

    public static void assertAmountLimits(CommonData commonData, Double expectedMinSumInsured, Double expectedMaxSumInsured, Double expectedMinPremium, Double expectedMaxPremium) {
        if (expectedMinSumInsured != null) {
            Assert.assertEquals(expectedMinSumInsured, commonData.getMinSumInsured());
        }
        if (expectedMaxSumInsured != null) {
            Assert.assertEquals(expectedMaxSumInsured, commonData.getMaxSumInsured());
        }
        if (expectedMinPremium != null) {
            Assert.assertEquals(expectedMinPremium, commonData.getMinPremium());
        }
        if (expectedMaxPremium != null) {
            Assert.assertEquals(expectedMaxPremium, commonData.getMaxPremium());
        }
    }

    public static void assertPremiumDataAfterQuoteCalculationWithFullDetail(PremiumsData premiumsData) {
        LOGGER.debug("\nPremiumsData: " + ObjectMapperUtil.toJson(new ObjectMapper(), premiumsData));
        assertAmountNotNull(premiumsData.getFinancialScheduler().getModalAmount());
        Assert.assertNotNull(premiumsData.getFinancialScheduler().getEndDate());
        Assert.assertNotNull(premiumsData.getFinancialScheduler().getPeriodicity());
    }

    public static void assertCommonDataAfterQuoteCalculationWithFullDetail(CommonData commonData) {
        LOGGER.debug("\nCommonData: " + ObjectMapperUtil.toJson(new ObjectMapper(), commonData));
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

    /**
     * Validate quote after calculation
     * If you don't want to validate any expected value, please input null to that value.
     *
     * @param quote
     * @param productIGenPremium
     * @param expectPremiumValue
     * @param expectTotalTaxDeduction
     * @param expectEndContractBenefit
     */
    public static void assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(Quote quote, ProductIGenPremium productIGenPremium, Double expectPremiumValue, Double expectTotalTaxDeduction, Double expectEndContractBenefit) {
        assertPremiumDataAfterQuoteCalculationWithFullDetail(quote.getPremiumsData());
        assertCommonDataAfterQuoteCalculationWithFullDetail(quote.getCommonData());

        LOGGER.debug("\nQuote: " + ObjectMapperUtil.toJson(new ObjectMapper(), quote));

        Amount actualPremium = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        Amount actualTotalTaxDeduction = productIGenPremium.getTotalTaxDeduction();
        Amount actualEndOfContractBenefit = productIGenPremium.getEndOfContractBenefit();
        assertAmountNotNull(actualPremium);
        assertAmountNotNull(actualTotalTaxDeduction);
        assertAmountNotNull(actualEndOfContractBenefit);

        if (expectPremiumValue != null) {
            Assert.assertEquals(expectPremiumValue, actualPremium.getValue(), DOUBLE_COMPARE_EXACT_VALUE);
        }
        if (expectTotalTaxDeduction != null) {
            Assert.assertEquals(expectTotalTaxDeduction, actualTotalTaxDeduction.getValue(), DOUBLE_COMPARE_EXACT_VALUE);
        }
        if (expectEndContractBenefit != null) {
            Assert.assertEquals(expectEndContractBenefit, actualEndOfContractBenefit.getValue(), DOUBLE_COMPARE_EXACT_VALUE);
        }
    }

    public static void assertAmountNotNull(Amount amount) {
        Assert.assertNotNull(amount);
        Assert.assertNotNull(amount.getValue());
        Assert.assertNotNull(amount.getCurrencyCode());
    }

    public static void assertPolicyAfterCreatingFromQuote(Policy policy) {
        Assert.assertNotNull(policy.getId());
        Assert.assertNotNull(policy.getPolicyId());
        assertPaymentsWithNoPayment(policy);
    }

    private static void assertPaymentsWithNoPayment(Policy policy) {
        List<Payment> payments = policy.getPayments();
        Assert.assertEquals((int) policy.getCommonData().getNbOfYearsOfPremium(), payments.size());
        LocalDateTime previousDueDate = null;
        for (Payment payment : payments) {
            Assert.assertEquals(policy.getPolicyId(), payment.getPolicyId());
            assertAmountNotNull(payment.getAmount());
            Assert.assertEquals(PaymentStatus.NOT_PROCESSED, payment.getStatus());
            Assert.assertNull(payment.getRegistrationKey());
            Assert.assertNull(payment.getEffectiveDate());
            Assert.assertNull(payment.getReceiptImageDocument());
            Assert.assertNull(payment.getReceiptPdfDocument());
            Assert.assertNull(payment.getOrderId());
            if (previousDueDate != null) {
                Assert.assertEquals(1, Period.between(previousDueDate.toLocalDate(), payment.getDueDate().toLocalDate()).getYears());
            }
            previousDueDate = payment.getDueDate();
        }
    }

    public static void assertDateTimeAmount(List<DateTimeAmount> dateTimeAmounts, double... amounts) {
        Assert.assertEquals(dateTimeAmounts.size(), amounts.length);
        int i = 0;
        for (DateTimeAmount dateTimeAmount : dateTimeAmounts) {
            Assert.assertEquals(amounts[i], dateTimeAmount.getAmount().getValue(), DOUBLE_COMPARE_EXACT_VALUE);
            i++;
        }
    }
}
