package th.co.krungthaiaxa.api.elife.policyNumbersQuota.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.data.PolicyNumbersQuotaNotification;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PolicyNumbersQuotaNotificationRepository extends MongoRepository<PolicyNumbersQuotaNotification, String> {
    Optional<PolicyNumbersQuotaNotification> findOneByNotificationEmail();
}
