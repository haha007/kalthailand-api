package th.co.krungthaiaxa.api.elife.test.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SaleIllustration10ECService;
import th.co.krungthaiaxa.api.elife.service.SaleIllustrationiFineService;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.beneficiary;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.products.ProductType.PRODUCT_IFINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SaleIllustrationServiceTest extends ELifeTest {

    @Inject
    private SaleIllustration10ECService saleIllustration10ECService;
    @Inject
    private SaleIllustrationiFineService saleIllustrationiFineService;
    @Inject
    private IProtectSaleIllustrationService iProtectSaleIllustrationService;
    @Inject
    private QuoteService quoteService;

    @Inject
    private QuoteFactory quoteFactory;
    private final String base64 = "";

    //TODO recheck logic here
    @Test
    public void should_generate_sale_illustration_iprotect_monthly_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, ProductQuotationFactory.constructIProtect(55, PeriodicityCode.EVERY_MONTH, 10000.0, false, 35, GenderCode.MALE));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Amount am = new Amount(1000.0, "THB");
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(am);
        quote.getPremiumsData().getProductIProtectPremium().setDeathBenefit(am);
        quote.getPremiumsData().getProductIProtectPremium().setSumInsured(am);
        quote.getPremiumsData().getProductIProtectPremium().setYearlyTaxDeduction(am);
        quote.getPremiumsData().getProductIProtectPremium().setTotalTaxDeduction(am);
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(PeriodicityCode.EVERY_MONTH);
        quote.getPremiumsData().getFinancialScheduler().setPeriodicity(periodicity);
        quote.getPremiumsData().getProductIProtectPremium().setSumInsured(am);

        Pair<byte[], String> pair = iProtectSaleIllustrationService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iProtect-sale-illustration_" + pair.getRight()), pair.getLeft());
    }

    @Test
    public void should_generate_sale_illustration_iprotect_yearly_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, ProductQuotationFactory.constructIProtect(55, PeriodicityCode.EVERY_YEAR, 100000.0, false, 35, GenderCode.MALE));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Amount am = new Amount(1000.0, "THB");
        quote.getPremiumsData().getFinancialScheduler().setModalAmount(am);
        quote.getPremiumsData().getProductIProtectPremium().setDeathBenefit(am);
        quote.getPremiumsData().getProductIProtectPremium().setSumInsured(am);
        quote.getPremiumsData().getProductIProtectPremium().setYearlyTaxDeduction(am);
        quote.getPremiumsData().getProductIProtectPremium().setTotalTaxDeduction(am);
        Periodicity periodicity = new Periodicity();
        periodicity.setCode(PeriodicityCode.EVERY_YEAR);
        quote.getPremiumsData().getFinancialScheduler().setPeriodicity(periodicity);
        quote.getPremiumsData().getProductIProtectPremium().setSumInsured(am);

        Pair<byte[], String> pair = iProtectSaleIllustrationService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iProtect-sale-illustration_" + pair.getRight()), pair.getLeft());

    }

    @Test
    public void should_generate_sale_illustration_10ec_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        Pair<byte[], String> pair = saleIllustration10ECService.generatePDF(quote, base64);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "10ec-sale-illustration_" + pair.getRight()), pair.getLeft());

    }

    @Test
    public void should_generate_sale_illustration_ifine_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        Pair<byte[], String> pair = saleIllustrationiFineService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iFine-sale-illustration_" + pair.getRight()), pair.getLeft());

    }

    @Test
    public void should_generate_sale_illustration_ifine_with_autopay_from_line_pay_wording_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 50, EVERY_MONTH, 10000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        Pair<byte[], String> pair = saleIllustrationiFineService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iFine-sale-illustration_" + pair.getRight()), pair.getLeft());

    }

}
