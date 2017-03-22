package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Document;

import java.util.List;

@Repository
public interface DocumentRepository extends PagingAndSortingRepository<Document, String> {
    List<Document> findByPolicyId(final String policyId);
}
