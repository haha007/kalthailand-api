package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;

import java.util.stream.Stream;

@Repository
public interface PolicyNumberRepository extends PagingAndSortingRepository<PolicyNumber, String> {
    Stream<PolicyNumber> findByPolicyNull();

    long countByPolicyNull();

    PolicyNumber findByPolicyId(String policyId);
}
