package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;

import java.util.stream.Stream;

@Repository
public interface PolicyNumberRepository extends MongoRepository<PolicyNumber, String> {
    Stream<PolicyNumber> findByPolicyNull();

    Page<PolicyNumber> findByPolicyNull(Pageable pageable);

    long countByPolicyNull();

    PolicyNumber findByPolicyId(String policyId);
}
