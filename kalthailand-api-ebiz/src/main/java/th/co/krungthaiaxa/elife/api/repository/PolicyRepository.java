package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.model.Policy;

@Repository
public interface PolicyRepository extends PagingAndSortingRepository<Policy, String> {
    Policy findByQuoteId(String quoteId);
}
