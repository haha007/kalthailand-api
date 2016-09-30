package th.co.krungthaiaxa.api.elife.products.igen;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.beneficiary;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.products.ProductType.PRODUCT_IFINE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.itextpdf.text.DocumentException;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.DateTimeAmount;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.SaleIllustrationiFineService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IGenPdfRenderServiceTest {
	
	@Inject
    private QuoteService quoteService;
	@Inject
	private IGenPdfRenderService iGenPdfRenderService;
	
	@Test
    public void should_generate_sale_illustration_pdf_file() throws DocumentException, IOException {
		ProductQuotation productQuotation = productQuotation(ProductType.PRODUCT_IGEN, 30, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 5, GenderCode.MALE);
		Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);
        Pair<byte[], String> pair = iGenPdfRenderService.generateSaleIllustrationPDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File("target/" + pair.getRight()), pair.getLeft());
    }

}
