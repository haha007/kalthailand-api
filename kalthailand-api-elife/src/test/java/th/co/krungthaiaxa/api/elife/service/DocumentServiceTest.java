package th.co.krungthaiaxa.api.elife.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.repository.DocumentDownloadRepository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM_VALIDATED;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.DA_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_IMAGE;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DocumentServiceTest extends ELifeTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyDocumentService policyDocumentService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyService policyService;
    @Inject
    private DocumentDownloadRepository documentDownloadRepository;
    @Inject
    private PolicyFactory policyFactory;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void should_add_2_documents_in_policy() {
        Policy policy = getPolicy(EVERY_MONTH);
        TestUtil.policy(policy);

        assertThat(policy.getDocuments()).hasSize(0);
        Document document1 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_IMAGE);
        Document document2 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_PDF);
        Optional<Policy> savedPolicy = policyService.findPolicyByPolicyNumber(policy.getPolicyId());
        assertThat(savedPolicy.get().getDocuments()).containsExactly(document1, document2);
    }

    @Test
    public void should_get_1_document_in_policy() {
        Policy policy = getPolicy(EVERY_MONTH);
        TestUtil.policy(policy);

        assertThat(policy.getDocuments()).hasSize(0);
        Document document = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/png", ERECEIPT_IMAGE);
        DocumentDownload documentDownload = documentDownloadRepository.findByDocumentId(document.getId());
        assertThat(document.getCreationDate()).isNotNull();
        assertThat(document.getId()).isNotNull();
        assertThat(document.getPolicyId()).isEqualTo(policy.getPolicyId());
        assertThat(document.getTypeName()).isEqualTo(ERECEIPT_IMAGE);
        assertThat(documentDownload.getContent()).isNotNull();
        assertThat(documentDownload.getDocumentId()).isEqualTo(document.getId());
        assertThat(documentDownload.getId()).isNotNull();
        assertThat(documentDownload.getMimeType()).isEqualTo("image/png");
        assertThat(documentDownload.getName()).isNull();
    }

    @Test
    public void should_have_2_documents_generated_when_policy_is_waiting_for_payment() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policyDocumentService.generateDocumentsForPendingValidation(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM);
    }

    @Test
    public void should_still_have_only_2_documents_even_after_generating_more_than_once() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policyDocumentService.generateDocumentsForPendingValidation(policy);
        policyDocumentService.generateDocumentsForPendingValidation(policy);
        policyDocumentService.generateDocumentsForPendingValidation(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM);
    }

    @Test
    public void should_have_4_documents_generated_when_policy_is_validated_and_not_monthly() throws Exception {
        Policy policy = getPolicy(EVERY_YEAR);
        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_have_5_documents_generated_when_policy_is_validated_and_monthly() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_still_have_only_5_documents_even_after_generating_more_than_once() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_create_bytes_for_eReceipt() throws IOException, DocumentException {
        Policy policy = getPolicy(EVERY_MONTH);
        TestUtil.policy(policy);

        policyDocumentService.generateValidatedPolicyDocuments(policy, "token");
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.findDocumentDownload(documentPdf.get().getId());
        byte[] decodedContent = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(decodedContent)).isNotNull();

        // Creates pdf in target folder
        File file = IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "documentServiceTest1-ereceipt.pdf", decodedContent);
        assertThat(file.exists()).isTrue();
    }

    private Policy getPolicy(PeriodicityCode periodicityCode) {
        ProductQuotation productQuotation = TestUtil.productQuotation(25, periodicityCode);
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, productQuotation);
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        return policyService.createPolicy(quote);
        //        return policyFactory.createPolicyWithValidatedStatus(productQuotation, "dummy.test.ags@gmail.com");

    }
}
