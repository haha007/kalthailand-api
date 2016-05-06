package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.data.BlackListed;

@Repository
public interface BlackListedRepository extends PagingAndSortingRepository<BlackListed, Integer> {
    BlackListed findByIdNumber(String idNumber);
    @Query("{ 'idNumber' : {$regex : '.*?0.*'}}")
    Page<BlackListed> findByIdNumberContaining(String idNumber, Pageable pageable);
}
