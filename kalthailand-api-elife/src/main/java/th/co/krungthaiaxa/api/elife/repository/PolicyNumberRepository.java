package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import org.springframework.data.domain.Page;

import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;

@Repository
public interface PolicyNumberRepository extends PagingAndSortingRepository<PolicyNumber, String> {
    Stream<PolicyNumber> findByPolicyNull();

    Page<PolicyNumber> findByPolicyNull(Pageable pageable);

    long countByPolicyNull();

    PolicyNumber findByPolicyId(String policyId);
}
