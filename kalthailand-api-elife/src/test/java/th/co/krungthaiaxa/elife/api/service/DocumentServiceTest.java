package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.DocumentDownloadRepository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DocumentServiceTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private DocumentDownloadRepository documentDownloadRepository;

    @Test
    public void should_add_2_documents_in_policy() {
        Policy policy = getPolicy(EVERY_MONTH);
        policy(policy);

        assertThat(policy.getDocuments()).hasSize(0);
        Document document1 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_IMAGE);
        Document document2 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_PDF);
        Optional<Policy> savedPolicy = policyService.findPolicy(policy.getPolicyId());
        assertThat(savedPolicy.get().getDocuments()).containsExactly(document1, document2);
    }

    @Test
    public void should_get_1_document_in_policy() {
        Policy policy = getPolicy(EVERY_MONTH);
        policy(policy);

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
        documentService.generateNotValidatedPolicyDocuments(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM);
    }

    @Test
    public void should_still_have_only_2_documents_even_after_generating_more_than_once() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        documentService.generateNotValidatedPolicyDocuments(policy);
        documentService.generateNotValidatedPolicyDocuments(policy);
        documentService.generateNotValidatedPolicyDocuments(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM);
    }

    @Test
    public void should_have_4_documents_generated_when_policy_is_validated_and_not_monthly() throws Exception {
        Policy policy = getPolicy(EVERY_YEAR);
        documentService.generateValidatedPolicyDocuments(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_have_5_documents_generated_when_policy_is_validated_and_monthly() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        documentService.generateValidatedPolicyDocuments(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_still_have_only_5_documents_even_after_generating_more_than_once() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        documentService.generateValidatedPolicyDocuments(policy);
        documentService.generateValidatedPolicyDocuments(policy);
        documentService.generateValidatedPolicyDocuments(policy);
        assertThat(policy.getDocuments()).extracting("typeName").containsExactly(APPLICATION_FORM, DA_FORM, APPLICATION_FORM_VALIDATED, ERECEIPT_IMAGE, ERECEIPT_PDF);
    }

    @Test
    public void should_create_bytes_for_eReceipt() throws IOException, DocumentException {
        Policy policy = getPolicy(EVERY_MONTH);
        policy(policy);

        documentService.generateValidatedPolicyDocuments(policy);
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] decodedContent = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(decodedContent)).isNotNull();

        // Creates pdf in target folder
        File file = new File("target/documentServiceTest1-ereceipt.pdf");
        FileUtils.writeByteArrayToFile(file, decodedContent);
        assertThat(file.exists()).isTrue();
    }

    private Policy getPolicy(PeriodicityCode periodicityCode) {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(25, periodicityCode));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
