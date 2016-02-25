package th.co.krungthaiaxa.elife.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.DocumentType;
import th.co.krungthaiaxa.elife.api.repository.DocumentDownloadRepository;
import th.co.krungthaiaxa.elife.api.repository.DocumentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentDownloadRepository documentDownloadRepository;
    private final PolicyRepository policyRepository;

    @Inject
    public DocumentService(DocumentRepository documentRepository, DocumentDownloadRepository documentDownloadRepository, PolicyRepository policyRepository) {
        this.documentRepository = documentRepository;
        this.documentDownloadRepository = documentDownloadRepository;
        this.policyRepository = policyRepository;
    }

    public DocumentDownload downloadDocument(String documentId) {
        return documentDownloadRepository.findByDocumentId(documentId);
    }

    public Document addDocument(Policy policy, byte[] encodedContent, String mimeType, DocumentType documentType) {
        LocalDateTime now = now(of(SHORT_IDS.get("VST")));

        Document document = new Document();
        document.setCreationDate(now);
        document.setPolicyId(policy.getPolicyId());
        document.setTypeName(documentType);
        document = documentRepository.save(document);

        DocumentDownload documentDownload = new DocumentDownload();
        documentDownload.setContent(new String(encodedContent, Charset.forName("UTF-8")));
        documentDownload.setDocumentId(document.getId());
        documentDownload.setMimeType(mimeType);
        documentDownloadRepository.save(documentDownload);

        policy.addDocument(document);
        policyRepository.save(policy);
        return document;
    }
}
