package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.model.Document;

@Repository
public interface DocumentRepository extends PagingAndSortingRepository<Document, String> {
}
