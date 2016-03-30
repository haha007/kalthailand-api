package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static th.co.krungthaiaxa.elife.api.TestUtil.beneficiary;
import static th.co.krungthaiaxa.elife.api.TestUtil.productQuotation;
import static th.co.krungthaiaxa.elife.api.TestUtil.quote;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;

import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

/**
 * Created by SantiLik on 3/28/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SaleIllustrationServiceTest {

    @Inject
    private SaleIllustration10ECService saleIllustration10ECService;
    @Inject
    private SaleIllustrationiFineService saleIllustrationiFineService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;

    private final String base64 = "";

    @Test
    public void should_generate_sale_illustration_10ec_pdf_file()throws  Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Pair<byte[], String> pair = saleIllustration10ECService.generatePDF(quote, base64);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(tmpPathDeletedAfterTests + "/" + pair.getRight()), pair.getLeft());
    }

    @Test
    public void should_generate_sale_illustration_ifine_pdf_file()throws  Exception{
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Pair<byte[], String> pair = saleIllustrationiFineService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(tmpPathDeletedAfterTests +"/" + pair.getRight()), pair.getLeft());
    }

    @Test
    public void should_generate_sale_illustration_ifine_with_autopay_from_line_pay_wording_pdf_file()throws  Exception{
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 50, EVERY_MONTH, 10000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Pair<byte[], String> pair = saleIllustrationiFineService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File(tmpPathDeletedAfterTests +"/" + pair.getRight()), pair.getLeft());
    }

}
