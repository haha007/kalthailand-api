package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByPolicyId(String policyId);

    @Query(value = "{"
            + " '$and':["
            + "      {'policyId': ?0}"
            + "     ,{'dueDate':{$gte: ?1, $lte: ?2}}"
            + "     ,{'status': ?3}"
            + " ]"
            + "}")
    Optional<Payment> findOneByPolicyIdAndDueDateRangeAndInStatus(String policyNumber, LocalDateTime searchFromDueDate, LocalDateTime searchToDueDate, PaymentStatus paymentStatus);

    @Query(value = "{"
            + " '$and':["
            + "      {'policyId': ?0}"
            + "     ,{'registrationKey': {'$ne': null}}"
            + " ]"
            + "}")
    Optional<Payment> findOneByRegKeyNotNullAndPolicyId(String policyNumber, Sort sort);

    @Query(value = "{'$and': [{'registrationKey':{'$ne': null}}, {'registrationKey':{'$ne': ''}}]}")
    List<Payment> findByRegKeyNotEmpty(Sort sort);

    @Query(value = "{'$and': [{'policyId':?0}, {'effectiveDate':{'$gt': ?1}}, {'status': ?2}]}")
    Payment findOneByPolicyAndNewerEffectiveDate(String policyId, LocalDateTime effectiveDate, PaymentStatus status);

    @Query(value = "{'$and': [{'policyId':?0}, {'_id':{'$gt': ?1}}, {'status': ?2}]}")
    Payment findOneByPolicyAndNewerId(String policyId, String paymentId, PaymentStatus status);

    @Query(value = "{'$and': [{'policyId':?0}, {'dueDate':{'$gt': ?1}}]}")
    Payment findOneByPolicyAndNewerDueDate(String policyId, LocalDateTime dueDate);

    List<Payment> findByPolicyIdAndTransactionIdNotNull(String policyId, Pageable pageable);
}
