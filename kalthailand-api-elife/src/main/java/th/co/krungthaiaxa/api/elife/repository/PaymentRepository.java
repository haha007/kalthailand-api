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

    List<Payment> findByPolicyId(String policyId, Sort sort);

    List<Payment> findByPolicyId(String policyId, Pageable pageable);

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

    List<Payment> findByRetryPaymentIdNotNull();

    List<Payment> findByRetryPaymentIdNotNull(Pageable pageable);

    @Query(value = "{'$and': [{'retryPaymentId':{'$ne': null}}, {'retryPaymentId':{'$ne': ''}}]}", fields = "{'retryPaymentId':1, 'paymentId':0}")
    List<Payment> findRetryPaymentIdByRetryPaymentIdNotNull();

    List<Payment> findByReceiptPdfDocumentNotNullAndReceiptNumberNull();

    List<Payment> findByPaymentIdIn(List<String> paymentIds);

    List<Payment> findByReceiptPdfDocumentNotNullAndReceiptNumberNullAndPaymentIdNotIn(List<String> paymentIds);

    List<Payment> findByReceiptPdfDocumentNotNullAndReceiptNumberNullAndPaymentIdIn(List<String> paymentIds);

    List<Payment> findByReceiptPdfDocumentNotNull();

    /**
     * @param receiptNumberOldPattern
     * @return the payments only contain receiptNumber and paymentId.
     */
    @Query(value = "{'$and': [{'receiptNumberOldPattern': ?0}, {'receiptNumber': {'$ne': null}}]}", fields = "{'receiptNumber':1, 'paymentId':1, 'policyId': 1}")
    List<Payment> findReceiptNumbersByReceiptNumberOldPatternAndReceiptNumberNotNull(boolean receiptNumberOldPattern);
}
