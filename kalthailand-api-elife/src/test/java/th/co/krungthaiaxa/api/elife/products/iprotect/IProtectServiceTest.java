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
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
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
    public void test_00_create_product_amount_no_birthday_success() {
        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(ProductType.PRODUCT_IPROTECT);
        productQuotation.setPeriodicityCode(PeriodicityCode.EVERY_YEAR);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(productAmounts));
    }

    @Test
    public void test_00_calculate_product_amount_success() {
        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(ProductType.PRODUCT_IPROTECT);
        productQuotation.setPackageName(IProtectPackage.IPROTECT10.name());
        productQuotation.setDateOfBirth(LocalDate.of(1990, 12, 2));
        productQuotation.setGenderCode(GenderCode.MALE);
        productQuotation.setOccupationId(null);
        productQuotation.setPeriodicityCode(PeriodicityCode.EVERY_YEAR);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(productAmounts));
        Assert.assertNotNull(productAmounts.getMaxSumInsured());
        Assert.assertNotNull(productAmounts.getMinPremium());
        Assert.assertNotNull(productAmounts.getMinSumInsured());
        Assert.assertNotNull(productAmounts.getMaxPremium());
//        Assert.assertNotNull(productAmounts.getCommonData().getMaxSumInsured());
//        Assert.assertNotNull(productAmounts.getCommonData().getMinPremium());
    }

    @Test
    public void test_01_create_quote() {
        //These testing numbers are calculated by Excel file from Business team. So the result from our program should match the number of business team.
        testPremiumToSumInsured(1000, 282798);
        testSumInsuredToPremium(282798, 1000);

    }

    @Test
    public void test_01_create_quote_with_discount() {
        //These testing numbers are calculated by Excel file from Business team. So the result from our program should match the number of business team.
        testPremiumToSumInsured(4500, 1272588.44, 1305824);
        testSumInsuredToPremium(1305824, 4500);
    }

    @Test
    public void test_01_create_quote_error_when_over_maximum_value() {
        testPremiumToSumInsured(6000, 4886625);//Too much sumInsured
        testSumInsuredToPremium(1500001, 16400);
    }

    private void testPremiumToSumInsured(double inputPremium, double expectSumInsured) {
        testPremiumToSumInsured(false, inputPremium, null, expectSumInsured);
    }

    private void testPremiumToSumInsured(double inputPremium, double expectSumInsuredBeforeDiscount, double expectSumInsured) {
        testPremiumToSumInsured(false, inputPremium, expectSumInsuredBeforeDiscount, expectSumInsured);
    }

    private void testSumInsuredToPremium(double inputSumInsured, double expectPremium) {
        testPremiumToSumInsured(true, inputSumInsured, null, expectPremium);
    }

    private void testSumInsuredToPremium(double inputSumInsured, double expectPremiumBeforDiscount, double expectPremium) {
        testPremiumToSumInsured(true, inputSumInsured, expectPremiumBeforDiscount, expectPremium);
    }

    private void testPremiumToSumInsured(boolean isInputSumInsured, double inputAmountValue, Double expectOutputAmountBeforeDiscount, double expectOutput) {
        int age = 32;
        PeriodicityCode periodicityCode = PeriodicityCode.EVERY_MONTH;
        int taxPercentage = 35;

        ProductQuotation productQuotation = TestUtil.productQuotation(
                ProductType.PRODUCT_IPROTECT,
                IProtectPackage.IPROTECT10.name(),
                age,
                periodicityCode,
                inputAmountValue, isInputSumInsured,
                taxPercentage,
                GenderCode.MALE);
        quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        LOGGER.debug(ObjectMapperUtil.toStringMultiLine(quote));

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

        Assert.assertEquals(inputAmountValue, calculatedInput.getValue(), AMOUNT_DELTA);

        Assert.assertEquals(expectOutput, Math.ceil(calculatedOutput.getValue()), AMOUNT_DELTA);
        if (expectOutputAmountBeforeDiscount != null) {
            Assert.assertEquals(expectOutputAmountBeforeDiscount, calculatedOutputBeforeDiscount.getValue(), AMOUNT_DELTA);
        }
        Assert.assertEquals(calculatedOutput.getCurrencyCode(), calculatedOutputBeforeDiscount.getCurrencyCode());
    }

    @Test
    public void test_02_01_update_quote() {
        if (quote == null) {
            test_01_create_quote();
        }
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
    }

    @Test
    public void test_02_02_create_policy_from_quote() {
        if (quote == null) {
            test_01_create_quote();
        }
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        Policy policy = policyService.createPolicy(quote);
        Assert.assertEquals(policy.getPolicyId(), quote.getPolicyId());
    }

    @Test
    public void test_calculate_quote_when_missing_both_premium_and_sumInsured() {
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
}
