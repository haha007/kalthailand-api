package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Policy;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    Policy findByQuoteId(String quoteId);

    Policy findByPolicyId(String policyId);
}
