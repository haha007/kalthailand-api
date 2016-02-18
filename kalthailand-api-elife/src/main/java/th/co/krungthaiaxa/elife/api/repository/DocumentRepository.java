package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import th.co.krungthaiaxa.elife.api.model.Document;

public interface DocumentRepository extends PagingAndSortingRepository<Document, String> {
}
