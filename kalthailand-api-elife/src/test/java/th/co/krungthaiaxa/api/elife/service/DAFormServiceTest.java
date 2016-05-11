package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DAFormServiceTest {
    @Inject
    private DAFormService DAFormService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_generate_da_form_pdf_file() throws Exception {
        Policy policy = getPolicy();

        byte[] pdfContent = DAFormService.generateDAForm(policy);
        File pdfFile = new File("target/da-form.pdf");
        writeByteArrayToFile(pdfFile, pdfContent);

        // check if file exist and can read as PDF
        assertThat(pdfFile.exists()).isTrue();
        assertThat(new PdfReader(pdfContent)).isNotNull();
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
