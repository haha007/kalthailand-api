package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import th.co.krungthaiaxa.elife.api.data.ThaiIdBlackList;

public interface ThaiIdBlackListRepository extends PagingAndSortingRepository<ThaiIdBlackList, Integer> {
    ThaiIdBlackList findByIdNumber(String idNumber);
    @Query("{ 'idNumber' : {$regex : '.*?0.*'}}")
    Page<ThaiIdBlackList> findByIdNumberContaining(String idNumber, Pageable pageable);
}
