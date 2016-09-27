package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.igen.IGenRate;

@Repository
public interface ProductIGenRateRepository extends PagingAndSortingRepository<IGenRate, String> {
    IGenRate findByGender(String gender);
}
