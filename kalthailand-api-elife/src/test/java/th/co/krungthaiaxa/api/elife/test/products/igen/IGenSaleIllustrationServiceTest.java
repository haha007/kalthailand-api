package th.co.krungthaiaxa.api.elife.test.products.igen;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.igen.IGenSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IGenSaleIllustrationServiceTest {

    @Inject
    private QuoteService quoteService;
    @Inject
    private IGenSaleIllustrationService iGenPdfRenderService;
    @Inject
    private QuoteFactory quoteFactory;
    @Test
    public void should_generate_sale_illustration_pdf_file() throws DocumentException, IOException {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment();
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);
        Pair<byte[], String> pair = iGenPdfRenderService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File("target/" + pair.getRight()), pair.getLeft());
    }

}
