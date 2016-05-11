package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;

@Repository
public interface DocumentDownloadRepository extends PagingAndSortingRepository<DocumentDownload, String> {
    DocumentDownload findByDocumentId(String documentId);
}
