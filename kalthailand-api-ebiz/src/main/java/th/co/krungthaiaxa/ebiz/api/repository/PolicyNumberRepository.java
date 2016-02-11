package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.data.PolicyNumber;

import java.util.stream.Stream;

@Repository
public interface PolicyNumberRepository extends PagingAndSortingRepository<PolicyNumber, String> {
    Stream<PolicyNumber> findByPolicyNull();
}
