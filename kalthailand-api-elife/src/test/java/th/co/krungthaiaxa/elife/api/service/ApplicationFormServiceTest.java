package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
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

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationFormServiceTest {
    @Inject
    private ApplicationFormService appService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;

    @Test
    public void should_generate_application_pdf_file() throws Exception {
        Policy policy = getPolicy();

        byte[] pdfContent = appService.generatePdfForm(policy);
        File pdfFile = new File(tmpPathDeletedAfterTests + File.separator + "ApplicationFormServiceTest.pdf");
        FileUtils.writeByteArrayToFile(pdfFile, pdfContent);
        Assertions.assertThat(pdfFile.exists()).isTrue();
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
