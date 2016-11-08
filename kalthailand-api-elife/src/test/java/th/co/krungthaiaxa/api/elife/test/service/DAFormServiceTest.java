package th.co.krungthaiaxa.api.elife.test.service;

import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.PhoneNumber;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DAFormServiceTest extends ELifeTest {
    @Inject
    private th.co.krungthaiaxa.api.elife.service.DAFormService DAFormService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_generate_da_form_pdf_file() throws Exception {
        Policy policy = getPolicy();
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("022222222");
        policy.getInsureds().get(0).getPerson().setHomePhoneNumber(phone);
        phone.setNumber("033333333");
        policy.getInsureds().get(0).getPerson().setWorkPhoneNumber(phone);
        phone.setNumber("0899999999");
        policy.getInsureds().get(0).getPerson().setMobilePhoneNumber(phone);

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
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        return policyService.createPolicy(quote);
    }
}
