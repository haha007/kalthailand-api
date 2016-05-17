package th.co.krungthaiaxa.api.elife.client;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SigningClientTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private SigningClient signingClient;

    @Test
    public void should_get_signed_application_form() throws IOException {
        Policy policy = getPolicy();
        documentService.generateNotValidatedPolicyDocuments(policy);
        Optional<Document> applicationFormPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        DocumentDownload documentDownload = documentService.downloadDocument(applicationFormPdf.get().getId());
        byte[] encodedSignedDocument = signingClient.getEncodedSignedPdfDocument(documentDownload.getContent().getBytes());
        assertThat(new PdfReader(Base64.getDecoder().decode(encodedSignedDocument))).isNotNull();

        FileUtils.writeByteArrayToFile(new File("target/signedApplicationFormThroughAPI.pdf"), Base64.getDecoder().decode(encodedSignedDocument));
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation(25, PeriodicityCode.EVERY_MONTH));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
