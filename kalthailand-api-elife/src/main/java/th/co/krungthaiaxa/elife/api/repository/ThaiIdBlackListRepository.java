package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import th.co.krungthaiaxa.elife.api.data.ThaiIdBlackList;

public interface ThaiIdBlackListRepository extends PagingAndSortingRepository<ThaiIdBlackList, Integer> {
    ThaiIdBlackList findByIdNumber(String idNumber);

}
