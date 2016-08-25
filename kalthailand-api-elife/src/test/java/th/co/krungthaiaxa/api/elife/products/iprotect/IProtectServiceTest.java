package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.CommonData;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class IProtectServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(IProtectServiceTest.class);
    private static final double AMOUNT_DELTA = 0.001;

    @Inject
    QuoteService quoteService;

    @Inject
    PolicyService policyService;

    @Inject
    IProtectDiscountRateExcelLoaderService iProtectDiscountRateExcelLoaderService;

    @Inject
    IProtectService productService;

    private static Quote quote = null;

    @Test
    public void test_01_createQuote_with_default_value_01() {
        //These testing numbers are calculated by Excel file from Business team. So the result from our program should match the number of business team.
//        testCreateQuotePremiumToSumInsured(1000, 282798.0);
        testCreateQuotePremiumToSumInsured(1351.8, 382286.0);
        Assert.assertEquals(5257.0, quote.getPremiumsData().getProductIProtectPremium().getYearlyTaxDeduction().getValue(), AMOUNT_DELTA);

    }

    @Test
    public void test_01_createQuote_with_default_value_02() {
        testCreateQuoteSumInsuredToPremium(282798, 1000.0);
    }

    @Test
    public void test_02_01_updateQuote() {
        if (quote == null) {
            test_01_createQuote_with_default_value_01();
        }
        quote = quoteService.updateQuote(quote, "token");
    }

    @Test
    public void test_02_02_createPolicy_from_quote() {
        if (quote == null) {
            test_01_createQuote_with_default_value_01();
        }
        TestUtil.quote(quote, TestUtil.beneficiary(45.2, "3101202780273"), TestUtil.beneficiary(54.8, "3120300153833"));
        Policy policy = policyService.createPolicy(quote);
        Assert.assertEquals(policy.getPolicyId(), quote.getPolicyId());
    }

    @Test
    public void test_calculateProductAmounts_no_birthday_success() {
        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(ProductType.PRODUCT_IPROTECT);
        productQuotation.setPackageName(IProtectPackage.IPROTECT10.name());
        productQuotation.setPeriodicityCode(PeriodicityCode.EVERY_YEAR);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(productAmounts));
    }

    @Test
    public void test_calculateProductAmounts_success() {
        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(ProductType.PRODUCT_IPROTECT);
        productQuotation.setPackageName(IProtectPackage.IPROTECT10.name());
        productQuotation.setDateOfBirth(LocalDate.of(1990, 12, 2));
        productQuotation.setGenderCode(GenderCode.MALE);
        productQuotation.setOccupationId(1);
        productQuotation.setPeriodicityCode(PeriodicityCode.EVERY_YEAR);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(productAmounts));

        Assert.assertNotNull(productAmounts.getMaxSumInsured());
        Assert.assertNotNull(productAmounts.getMinPremium());
        Assert.assertNotNull(productAmounts.getMinSumInsured());
        Assert.assertNotNull(productAmounts.getMaxPremium());

        CommonData commonData = productAmounts.getCommonData();
        Assert.assertNotNull(commonData.getMaxSumInsured());
        Assert.assertNotNull(commonData.getMinPremium());
        Assert.assertNotNull(commonData.getMinSumInsured());
        Assert.assertNotNull(commonData.getMaxPremium());
    }

    @Test
    public void test_createQuote_with_max_and_min_input_amounts() {
        //These testing numbers are calculated by Excel file from Business team. So the result from our program should match the number of business team.
        ProductQuotation productQuotation = createDefaultProductQuotation();
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        LOGGER.debug("ProductAmounts:\n" + ObjectMapperUtil.toStringMultiLine(productAmounts.getCommonData()));
        testCreateQuotePremiumToSumInsured(Math.ceil(productAmounts.getCommonData().getMinPremium().getValue()), null);
        testCreateQuotePremiumToSumInsured(Math.floor(productAmounts.getCommonData().getMaxPremium().getValue()), null);
        testCreateQuoteSumInsuredToPremium(Math.floor(productAmounts.getCommonData().getMaxSumInsured().getValue()), null);
        testCreateQuoteSumInsuredToPremium(Math.ceil(productAmounts.getCommonData().getMinSumInsured().getValue()), null);
    }

    @Test
    public void test_createQuote_success_when_missing_both_premium_and_sumInsured() {
        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(ProductType.PRODUCT_IPROTECT);
        productQuotation.setPackageName(IProtectPackage.IPROTECT10.name());
        productQuotation.setDateOfBirth(LocalDate.of(1990, 12, 2));
        productQuotation.setGenderCode(GenderCode.MALE);
        productQuotation.setOccupationId(null);
        productQuotation.setSumInsuredAmount(null);
        productQuotation.setPremiumAmount(new Amount());
        productQuotation.setPeriodicityCode(PeriodicityCode.EVERY_YEAR);
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(quote));
        Assert.assertNotNull(quote);
        ProductIProtectPremium iProtectPremium = quote.getPremiumsData().getProductIProtectPremium();
        Assert.assertNull(iProtectPremium.getSumInsured());
        Assert.assertNull(iProtectPremium.getSumInsuredBeforeDiscount());
        Assert.assertNull(iProtectPremium.getDeathBenefit());
        Assert.assertNull(iProtectPremium.getTotalTaxDeduction());
        Assert.assertNull(iProtectPremium.getYearlyTaxDeduction());
    }

    @Test
    public void test_createQuote_with_discount() {
        //These testing numbers are calculated by Excel file from Business team. So the result from our program should match the number of business team.
        testCreateQuotePremiumToSumInsured(4500, 1272588.44, 1305824.0);
        testCreateQuoteSumInsuredToPremium(1305824, 4500.0);
    }

    @Test(expected = QuoteCalculationException.class)
    public void test_createQuote_error_when_premium_bigger_maximum_value() {
        createQuote(false, 6000);
    }

    @Test(expected = QuoteCalculationException.class)
    public void test_createQuote_error_when_sumInsured_bigger_maximum_value() {
        createQuote(true, 1500001);
    }

    private void testCreateQuotePremiumToSumInsured(double inputPremium, Double expectSumInsured) {
        testCreateQuote(false, inputPremium, null, expectSumInsured);
    }

    private void testCreateQuotePremiumToSumInsured(double inputPremium, Double expectSumInsuredBeforeDiscount, Double expectSumInsured) {
        testCreateQuote(false, inputPremium, expectSumInsuredBeforeDiscount, expectSumInsured);
    }

    private void testCreateQuoteSumInsuredToPremium(double inputSumInsured, Double expectPremium) {
        testCreateQuote(true, inputSumInsured, null, expectPremium);
    }

    private void testCreateQuoteSumInsuredToPremium(double inputSumInsured, Double expectPremiumBeforDiscount, Double expectPremium) {
        testCreateQuote(true, inputSumInsured, expectPremiumBeforDiscount, expectPremium);
    }

    private void testCreateQuote(boolean isInputSumInsured, double inputAmountValue, Double expectOutputAmountBeforeDiscount, Double expectOutput) {
        quote = createQuote(isInputSumInsured, inputAmountValue);
        assertSumInsuredAndPremiumCalculation(isInputSumInsured, inputAmountValue, expectOutputAmountBeforeDiscount, expectOutput);
        assertEnoughData(quote);
    }

    private void assertEnoughData(Quote quote) {
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);

        //Check occupation Information
        Assert.assertNotNull(mainInsured.getProfessionId());
        Assert.assertNotNull(mainInsured.getProfessionName());
    }

    private void assertSumInsuredAndPremiumCalculation(boolean isInputSumInsured, double inputAmountValue, Double expectOutputAmountBeforeDiscount, Double expectOutput) {

        //Get result values
        Amount premiumAmountBeforeDiscount = quote.getPremiumsData().getFinancialScheduler().getModalAmountBeforeDiscount();
        Amount premiumAmount = quote.getPremiumsData().getFinancialScheduler().getModalAmount();
        Amount sumInsured = quote.getPremiumsData().getProductIProtectPremium().getSumInsured();
        Amount sumInsuredBeforeDiscount = quote.getPremiumsData().getProductIProtectPremium().getSumInsuredBeforeDiscount();

        Amount calculatedInput;
        Amount calculatedOutput;
        Amount calculatedOutputBeforeDiscount;
        if (isInputSumInsured) {
            calculatedInput = sumInsured;
            calculatedOutput = premiumAmount;
            calculatedOutputBeforeDiscount = premiumAmountBeforeDiscount;
        } else {
            calculatedInput = premiumAmount;
            calculatedOutput = sumInsured;
            calculatedOutputBeforeDiscount = sumInsuredBeforeDiscount;
        }

        //Assert
        Assert.assertEquals(inputAmountValue, calculatedInput.getValue(), AMOUNT_DELTA);

        if (expectOutput != null) {
            Assert.assertEquals(expectOutput, Math.ceil(calculatedOutput.getValue()), AMOUNT_DELTA);
        }
        if (expectOutputAmountBeforeDiscount != null) {
            Assert.assertEquals(expectOutputAmountBeforeDiscount, calculatedOutputBeforeDiscount.getValue(), AMOUNT_DELTA);
        }

        Assert.assertEquals(calculatedOutput.getCurrencyCode(), calculatedOutputBeforeDiscount.getCurrencyCode());
    }

    private Quote createQuote(boolean isInputSumInsured, double inputAmountValue) {
        ProductQuotation productQuotation = createDefaultProductQuotation(isInputSumInsured, inputAmountValue);
        LOGGER.debug("ProductQuotation:\n" + ObjectMapperUtil.toStringMultiLine(productQuotation));
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(quote));
        return quote;
    }

    private ProductQuotation createDefaultProductQuotation() {
        int age = 32;
        PeriodicityCode periodicityCode = PeriodicityCode.EVERY_MONTH;
        int taxPercentage = 35;

        return TestUtil.productQuotation(
                ProductType.PRODUCT_IPROTECT,
                IProtectPackage.IPROTECT10.name(),
                age,
                periodicityCode,
                null, true,
                taxPercentage,
                GenderCode.MALE);
    }

    private ProductQuotation createDefaultProductQuotation(boolean isInputSumInsured, double inputAmountValue) {
        ProductQuotation productQuotation = createDefaultProductQuotation();
        Amount inputAmount = ProductUtils.amountTHB(inputAmountValue);
        if (isInputSumInsured) {
            productQuotation.setSumInsuredAmount(inputAmount);
        } else {
            productQuotation.setPremiumAmount(inputAmount);
        }
        return productQuotation;
    }
}
