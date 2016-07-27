package th.co.krungthaiaxa.api.elife.policyNumbersQuota.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumbersQuotaNotification;

import java.util.Optional;

@Repository
public interface PolicyNumbersQuotaNotificationRepository extends MongoRepository<PolicyNumbersQuotaNotification, String> {
    Optional<PolicyNumbersQuotaNotification> findOneByNotificationEmail();
}
