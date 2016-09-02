package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

import java.time.LocalDate;
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
    Optional<Payment> findOneByPolicyIdAndDueDateRangeAndInStatus(String policyNumber, LocalDate searchFromDueDate, LocalDate searchToDueDate, PaymentStatus paymentStatus);

    @Query(value = "{"
            + " '$and':["
            + "      {'policyId': ?0}"
            + "     ,{'registrationKey': {'$ne': null}}"
            + " ]"
            + "}")
    Optional<Payment> findOneByRegKeyNotNullAndPolicyId(String policyNumber, Sort sort);

    @Query(value = "{'$and': [{'registrationKey':{'$ne': null}}, {'registrationKey':{'$ne': ''}}]}")
    List<Payment> findByRegKeyNotEmpty(Sort sort);
}
