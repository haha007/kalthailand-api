package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;

@Repository
public interface DocumentDownloadRepository extends PagingAndSortingRepository<DocumentDownload, String> {
    DocumentDownload findByDocumentId(String documentId);
}
