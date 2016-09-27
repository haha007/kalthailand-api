package th.co.krungthaiaxa.api.elife.products.igen;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.beneficiary;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.TestUtil.quote;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

import java.io.File;
import java.io.IOException;

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
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IGenSaleIllustrationServiceTest {
	
	@Inject
    private QuoteService quoteService;
	@Inject
	private IGenSaleIllustrationService iGenSaleIllustrationService;
	
	@Test
    public void should_generate_sale_illustration_pdf_file() throws DocumentException, IOException {
		Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(ProductType.PRODUCT_IPROTECT, 55, EVERY_YEAR, 100000.0));
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

        Pair<byte[], String> pair = iGenSaleIllustrationService.generatePDF(quote);
        assertThat(pair.getLeft()).isNotEmpty();
        assertThat(pair.getRight()).isNotEmpty();
        FileUtils.writeByteArrayToFile(new File("target/" + pair.getRight()), pair.getLeft());
    }

}
