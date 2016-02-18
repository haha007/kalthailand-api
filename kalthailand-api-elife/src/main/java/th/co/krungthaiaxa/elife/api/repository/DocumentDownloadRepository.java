package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;

public interface DocumentDownloadRepository extends PagingAndSortingRepository<DocumentDownload, String> {
    DocumentDownload findByDocumentId(String documentId);
}
