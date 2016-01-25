package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.model.Policy;

@Repository
public interface PolicyRepository extends PagingAndSortingRepository<Policy, String> {
    Policy findByQuoteFunctionalId(String quoteId);
}
