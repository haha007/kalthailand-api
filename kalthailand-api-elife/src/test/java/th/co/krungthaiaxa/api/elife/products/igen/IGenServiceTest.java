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
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductAssertUtil;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;
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
    private ProductPremiumRateRepository productPremiumRateRepository;

    @Test
    public void createCreateDefaultPremiumRateIfNotExist() {
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
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(null, null, null, null, null, null);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithSumInsureLimits(productAmounts);
    }

    @Test
    public void create_product_amount_success_with_full_default_data() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(15, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        ProductAmounts productAmounts = productService.calculateProductAmounts(productQuotation);
        ProductAssertUtil.assertProductAmountsWithFullDetail(productAmounts);
    }

    @Test
    public void create_quote_success_with_dividend_annual_payback() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(33, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        Quote quote = quoteService.createQuote(QuoteFactory.generateSession(), ChannelType.LINE, productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1980000.0);
    }

    @Test
    public void create_quote_success_with_dividend_annual_nextpremium() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(33, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_NEXT_PREMIUM);
        Quote quote = quoteService.createQuote(QuoteFactory.generateSession(), ChannelType.LINE, productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1980000.0);
    }

    @Test
    public void create_quote_success_with_dividend_end_of_contract() {
        ProductQuotation productQuotation = ProductQuotationFactory.initIGen(33, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
        Quote quote = quoteService.createQuote(QuoteFactory.generateSession(), ChannelType.LINE, productQuotation);
        ProductAssertUtil.assertQuoteWithPremiumAmountAndTaxAndEndContractBenefit(quote, getSpecificPremiumData(quote), 308000.0, 210000.0, 1998994.0);
    }

    private ProductIGenPremium getSpecificPremiumData(Quote quote) {
        return quote.getPremiumsData().getProductIGenPremium();
    }
}
