package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    Policy findByQuoteId(String quoteId);

    Policy findByPolicyId(String policyId);

    @Query("{$and: [{'premiumsData.financialScheduler.periodicity.code': ?0},{'premiumsData.financialScheduler.periodicity.code': {$exists: false}}]}")
    List<Policy> findByPeriodicityCodeAndAtpModeNull(PeriodicityCode periodicityCode);

    @Query("{'insureds.insuredPreviousInformations': { $gt: [] }}")
    List<Policy> findByInsuredPreviousPolicyNotNull(Pageable pageRequest);

    @Query(value = "{$and:[{'insureds.person.lineId': {$exists: true, $ne: ''}}," +
            "{$or:[{'insureds.person.lineUserId': {$exists: false}}, {'insureds.person.lineUserId': {$eq:''}}]}]}",
            fields = "{'policyId': 1,'insureds.person.lineId': 1}")
    List<Policy> findAllUserHaveNoLineUserId();

    @Query(value = "{$and:[{'status': '?0'}, {'insureds.startDate': ?1}]}")
    List<Policy> findAllPolicyByStatusOnDate(final PolicyStatus policyStatus, final LocalDate onDate);
}
