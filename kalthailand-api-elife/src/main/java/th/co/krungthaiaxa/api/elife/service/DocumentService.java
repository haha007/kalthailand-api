package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.DocumentReferenceType;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.repository.DocumentDownloadRepository;
import th.co.krungthaiaxa.api.elife.repository.DocumentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;

//TODO need to be refactored.
@Service
public class DocumentService {
    public final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentDownloadRepository documentDownloadRepository;
    private final PolicyRepository policyRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentDownloadRepository documentDownloadRepository, PolicyRepository policyRepository) {
        this.documentRepository = documentRepository;
        this.documentDownloadRepository = documentDownloadRepository;
        this.policyRepository = policyRepository;
    }

    public DocumentDownload findDocumentDownload(String documentId) {
        return documentDownloadRepository.findByDocumentId(documentId);
    }

    public byte[] getDocumentDownloadContent(DocumentDownload documentDownload) {
        return Base64.getDecoder().decode(documentDownload.getContent());
    }

    /**
     * This moethods encodes the content in Base64 by default
     *
     * @param policy
     * @param decodedContent
     * @param mimeType
     * @param documentType
     * @return
     */
    public Document addDocument(Policy policy, byte[] decodedContent, String mimeType, DocumentType documentType) {
        return addDocument(policy, decodedContent, mimeType, documentType, null, null);
    }

    /**
     * @param policy
     * @param decodedContent
     * @param mimeType
     * @param documentType
     * @param documentReferenceType this can be either the policy, or the quote,...
     * @param referenceId           this can be either the policyId,or quoteId...
     * @return
     */
    public Document addDocument(Policy policy, byte[] decodedContent, String mimeType, DocumentType documentType, DocumentReferenceType documentReferenceType, String referenceId) {
        LocalDateTime now = DateTimeUtil.nowLocalDateTimeInThaiZoneId();

        Document document = new Document();
        document.setCreationDate(now);
        document.setPolicyId(policy.getPolicyId());
        document.setTypeName(documentType);
        document.setReferenceType(documentReferenceType);
        document.setReferenceId(referenceId);
        document = documentRepository.save(document);

        DocumentDownload documentDownload = new DocumentDownload();
        documentDownload.setContent(new String(Base64.getEncoder().encode(decodedContent), Charset.forName("UTF-8")));
        documentDownload.setDocumentId(document.getId());
        documentDownload.setMimeType(mimeType);
        documentDownloadRepository.save(documentDownload);

        policy.addDocument(document);
        policy.setLastUpdateDateTime(Instant.now());
        policyRepository.save(policy);
        return document;
    }

}
