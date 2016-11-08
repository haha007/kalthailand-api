package th.co.krungthaiaxa.api.elife.test.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyDocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SigningClientTest extends ELifeTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyDocumentService policyDocumentService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private SigningClient signingClient;
    @Inject
    private PolicyFactory policyFactory;

    @Test
    public void should_get_signed_application_form() throws IOException {
        Policy policy = policyFactory.createPolicyWithPendingValidationStatus(ProductQuotationFactory.constructIGenDefault());
        policyDocumentService.generateDocumentsForPendingValidationPolicy(policy);
        Optional<Document> applicationFormPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        DocumentDownload documentDownload = documentService.findDocumentDownload(applicationFormPdf.get().getId());
        byte[] encodedSignedDocument = signingClient.getEncodedSignedPdfDocument(documentDownload.getContent().getBytes(), "token");
        assertThat(encodedSignedDocument).isNotNull();
    }

}
