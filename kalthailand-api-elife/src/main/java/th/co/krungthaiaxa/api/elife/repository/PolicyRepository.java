package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import java.util.List;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    Policy findByQuoteId(String quoteId);

    Policy findByPolicyId(String policyId);

    List<Policy> findByPremiumsDataNull();

    long countByPremiumsDataNull();

    @Query("{$and: [{'premiumsData.financialScheduler.periodicity.code': ?0},{'premiumsData.financialScheduler.periodicity.code': {$exists: false}}]}")
    List<Policy> findByPeriodicityCodeAndAtpModeNull(PeriodicityCode periodicityCode);
}
