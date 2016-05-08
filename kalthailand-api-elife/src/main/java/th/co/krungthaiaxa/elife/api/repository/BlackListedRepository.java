package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.data.BlackListed;

@Repository
public interface BlackListedRepository extends PagingAndSortingRepository<BlackListed, String> {
    BlackListed findByIdNumber(String idNumber);
    @Query("{ idNumber : ?0, name : ?1}")
    BlackListed findByIdNumberAndName(String idNumber, String name);
    Page<BlackListed> findByIdNumberContaining(String idNumber, Pageable pageable);
}
