package th.co.krungthaiaxa.api.elife.test.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.igen.IGenSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SaleIllustration10ECService;
import th.co.krungthaiaxa.api.elife.service.SaleIllustrationiFineService;
import th.co.krungthaiaxa.api.elife.service.SigningDocumentService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import java.io.File;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.products.ProductType.PRODUCT_IFINE;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.beneficiary;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.quote;

/**
 * @author tuong.le on 11/16/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SigningDocumentServiceTest extends ELifeTest {

    @Autowired
    private SigningDocumentService signingDocumentService;

    @Autowired
    QuoteService quoteService;

    @Autowired
    private IProtectSaleIllustrationService iProtectSaleIllustrationService;

    @Autowired
    private IGenSaleIllustrationService iGenSaleIllustrationService;

    @Autowired
    private SaleIllustrationiFineService saleIllustrationiFineService;

    @Autowired
    private SaleIllustration10ECService saleIllustration10ECService;

    @Test
    public void should_sign_proposal_pdf_iProtectSaleIllustration_and_store_test_result() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE,
                ProductQuotationFactory.constructIProtect(55, PeriodicityCode.EVERY_MONTH, 10000.0,
                        false, 35, GenderCode.MALE));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");
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
        byte[] signedPDF = signingDocumentService.signPDFFile(pair.getLeft(), RequestFactory.generateAccessToken());
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iProtect-sale-illustration_" + pair.getRight()), signedPDF);

    }

    @Test
    public void should_sign_proposal_iGenSaleIllustration_monthly_and_store_test_result() throws Exception {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment();
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);

        Pair<byte[], String> pair = iGenSaleIllustrationService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        byte[] signedPDF = signingDocumentService.signPDFFile(pair.getLeft(), RequestFactory.generateAccessToken());
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iGenSaleIllustration_" + pair.getRight()), signedPDF);
    }

    @Test
    public void should_generate_sale_illustration_ifine_signed_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        Pair<byte[], String> pair = saleIllustrationiFineService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        byte[] signedPDF = signingDocumentService.signPDFFile(pair.getLeft(), RequestFactory.generateAccessToken());
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "iFine-sale-illustration_" + pair.getRight()), signedPDF);
    }

    @Test
    public void should_generate_sale_illustration_10ec_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, RequestFactory.generateAccessToken());

        Pair<byte[], String> pair = saleIllustration10ECService.generatePDF(quote, "");
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();

        byte[] signedPDF = signingDocumentService.signPDFFile(pair.getLeft(), RequestFactory.generateAccessToken());
        FileUtils.writeByteArrayToFile(new File(TestUtil.PATH_TEST_RESULT + "10ec-sale-illustration_" + pair.getRight()), signedPDF);

    }
}
