package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.products.Product10EC;
import th.co.krungthaiaxa.elife.api.repository.DocumentDownloadRepository;
import th.co.krungthaiaxa.elife.api.repository.DocumentRepository;

import javax.inject.Inject;
import java.util.Base64;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_IMAGE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

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
    private DocumentRepository documentRepository;
    @Inject
    private DocumentDownloadRepository documentDownloadRepository;
    private Product10EC product10EC = new Product10EC();

    @Test
    public void should_add_2_documents_in_policy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote1 = quoteService.createQuote(randomNumeric(20), product10EC.getCommonData(), LINE, productQuotation());
        quote(quote1, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);
        Policy policy = policyService.createPolicy(quote1);
        assertThat(policy.getDocuments()).hasSize(0);

        Document document1 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_IMAGE);
        Document document2 = documentService.addDocument(policy, Base64.getEncoder().encode("something".getBytes()), "image/jpg", ERECEIPT_PDF);
        Policy savedPolicy = policyService.findPolicy(policy.getPolicyId());

        assertThat(savedPolicy.getDocuments()).containsExactly(document1, document2);
    }

    @Test
    public void should_get_1_document_in_policy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote1 = quoteService.createQuote(randomNumeric(20), product10EC.getCommonData(), LINE, productQuotation());
        quote(quote1, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);
        Policy policy = policyService.createPolicy(quote1);
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
}
