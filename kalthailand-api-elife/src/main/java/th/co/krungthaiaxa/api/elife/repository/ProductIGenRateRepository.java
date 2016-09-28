package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.OldIGenRate;

@Repository
public interface ProductIGenRateRepository extends PagingAndSortingRepository<OldIGenRate, String> {
    OldIGenRate findByGender(String gender);
}
