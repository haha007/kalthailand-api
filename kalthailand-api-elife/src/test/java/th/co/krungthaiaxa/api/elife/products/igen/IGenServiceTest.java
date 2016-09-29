package th.co.krungthaiaxa.api.elife.products.igen;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.factory.InsuredFactory;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductAssertUtil;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class IGenServiceTest extends ELifeTest {
    public static final ProductType PRODUCT_TYPE = ProductType.PRODUCT_IGEN;

    @Autowired
    private IGenService productService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private PolicyService policyService;

    @Autowired
    private ProductPremiumRateRepository productPremiumRateRepository;

    @Test
    public void createDefaultPremiumRateIfNotExist() {
        Optional<ProductPremiumRate> productPremiumRateOptional = productPremiumRateRepository.findOneByProductId(PRODUCT_TYPE.getLogicName());
        if (!productPremiumRateOptional.isPresent()) {
            ProductPremiumRate productPremiumRate = new ProductPremiumRate();
            productPremiumRate.setProductId(PRODUCT_TYPE.getLogicName());
            productPremiumRate.setPremiumRate(308.0);
            productPremiumRateRepository.save(productPremiumRate);
        }
    }

    @Test
    public void create_product_amount_success_wiht_only_productType() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(null, null, null, null, null, null);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithSumInsureLimits(productAmounts);
    }

    @Test
    public void create_product_amount_success_with_full_default_data() {
        ProductQuotation productQuotation = constructDefaultIGen();
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithFullDetail(productAmounts);
        ProductAssertUtil.assertAmountLimits(productAmounts, 100000.0, 1500000.0, 30800.0, 462000.0);
    }

    @Test
    public void create_quote_success_with_dividend_annual_payback() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        Quote quote = createAndFindQuote(productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1980000.0);
    }

    @Test
    public void create_quote_success_with_dividend_annual_nextpremium() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_NEXT_PREMIUM);
        Quote quote = createAndFindQuote(productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1980000.0);
    }

    @Test
    public void create_quote_success_with_dividend_end_of_contract() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
        Quote quote = createAndFindQuote(productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1998994.42);
    }

    @Test
    public void update_quote_success_without_changing_any_thing() {
        ProductQuotation productQuotation = constructDefaultIGen();
        Quote quote = createAndFindQuote(productQuotation);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        assertDefaultCalculationNumbersAreCorrect(quote);
    }

    @Test
    public void update_quote_success_with_new_beneficiaries() {
        ProductQuotation productQuotation = constructDefaultIGen();
        Quote quote = createAndFindQuote(productQuotation);
        InsuredFactory.setDefaultValuesToMainInsuredAnd2Beneficiaries(quote);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        assertDefaultCalculationNumbersAreCorrect(quote);
    }

    @Test
    public void create_policy_success_with_new_beneficiaries() {
        ProductQuotation productQuotation = constructDefaultIGen();
        Quote quote = createAndFindQuote(productQuotation);
        InsuredFactory.setDefaultValuesToMainInsuredAnd2Beneficiaries(quote);
        quote = quoteService.updateQuote(quote, RequestFactory.generateAccessToken());
        assertDefaultCalculationNumbersAreCorrect(quote);

        Policy policy = policyService.createPolicy(quote);
        ProductAssertUtil.assertPolicyAfterCreatingFromQuote(policy);
    }

    private Quote createAndFindQuote(ProductQuotation productQuotation) {
        String sessionQuote = RequestFactory.generateSession();
        Quote quote = quoteService.createQuote(sessionQuote, ChannelType.LINE, productQuotation);
        return quoteService.findByQuoteId(quote.getQuoteId(), sessionQuote, ChannelType.LINE).get();
    }

    private ProductIGenPremium getSpecificPremiumData(Quote quote) {
        return quote.getPremiumsData().getProductIGenPremium();
    }

    /**
     * The input here must match with result from {@link #assertDefaultCalculationNumbersAreCorrect(Quote)} && {@link #assertDefaultCalculationYearlyPaybackForIGen(Quote)}
     *
     * @return
     */
    private ProductQuotation constructDefaultIGen() {
        return ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
    }

    /**
     * The result here must match with the input from {@link #constructDefaultIGen()}
     *
     * @param quote
     */
    private void assertDefaultCalculationNumbersAreCorrect(Quote quote) {
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1998994.42);
        ProductAssertUtil.assertAmountLimits(quote.getCommonData(), 100000.0, 1500000.0, 30800.0, 462000.0);
        assertDefaultCalculationYearlyPaybackForIGen(quote);
    }

    /**
     * The result here must match with the input from {@link #constructDefaultIGen()}
     *
     * @param quote
     */
    private void assertDefaultCalculationYearlyPaybackForIGen(Quote quote) {
        ProductIGenPremium productIGenPremium = quote.getPremiumsData().getProductIGenPremium();
        ProductAssertUtil.assertDateTimeAmount(productIGenPremium.getYearlyCashBacksForAnnual(),
                20000.0
                , 40000.0
                , 60000.0
                , 80000.0
                , 100000.0
                , 120000.0
                , 140000.0
                , 160000.0
                , 180000.0
                , 1980000.0
        );
        ProductAssertUtil.assertDateTimeAmount(productIGenPremium.getYearlyCashBacksForEndOfContract(),
                20000.0000
                , 40400.0000
                , 61208.0000
                , 82432.1600
                , 104080.8032
                , 126162.4193
                , 148685.6676
                , 171659.3810
                , 195092.5686
                , 1998994.4200
        );
        ProductAssertUtil.assertDateTimeAmount(productIGenPremium.getYearlyDeathBenefits(),
                1000000.0
                , 1000000.0
                , 1000000.0
                , 1232000.0
                , 1540000.00
                , 1848000.00
                , 1848000.00
                , 1848000.00
                , 1848000.00
                , 1848000.00
        );
    }
}
