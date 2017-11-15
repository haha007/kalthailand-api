package th.co.krungthaiaxa.api.elife.test.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyDocumentService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.client.SigningClient.PASSWORD_DOB_PATTERN;
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

    @Test
    public void should_get_signed_application_with_password() throws IOException {
        Policy policy = policyFactory.createPolicyWithPendingValidationStatus(ProductQuotationFactory.constructIGenDefault());
        policyDocumentService.generateDocumentsForPendingValidationPolicy(policy);
        Optional<Document> applicationFormPdf = policy
                .getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        DocumentDownload documentDownload = documentService.findDocumentDownload(applicationFormPdf.get().getId());

        final Insured mainInssured = ProductUtils.validateExistMainInsured(policy);

        final String passwordProtected =
                mainInssured.getPerson().getBirthDate().format(DateTimeFormatter.ofPattern(PASSWORD_DOB_PATTERN));
        byte[] encodedSignedDocument = signingClient.getEncodedSignedPdfWithPassword(
                documentDownload.getContent().getBytes(), passwordProtected, "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbGlmZWFkbWludXNlciIsInJvbGUiOlsiVUlfU0xDIiwiQVBJX0JMQUNLTElTVCIsIlVJX0VMSUZFX0FETUlOIiwiVUlfQVVUT1BBWSIsIkFQSV9TSUdOSU5HIiwiVUlfVkFMSURBVElPTiIsIkFQSV9FTElGRSJdLCJjcmVhdGVkIjoiMjAxNy0xMS0xNVQxNjoxMDo0MC42NjIiLCJleHAiOjE1MTA3NDA2NDB9._aP7gvuT1NyMIT4Z2iE79u-NCFJlCO52fZYOCfPeARP3_qjpCBxk2JbNv7gfApRxIf2SYF9KZSL7vhv2Ol_jGg");
        byte[] decodedSignedPdf = Base64.getDecoder().decode(encodedSignedDocument);
        final String fileName = TestUtil.PATH_TEST_RESULT + System.currentTimeMillis()
                + "_ereceipt_password_" + policy.getPolicyId() + ".pdf";
        IOUtil.writeBytesToRelativeFile(fileName, decodedSignedPdf);
        assertThat(encodedSignedDocument).isNotNull();
    }

}
