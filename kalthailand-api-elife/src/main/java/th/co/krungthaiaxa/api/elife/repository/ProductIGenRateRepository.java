package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.ProductIGenRate;

@Repository
public interface ProductIGenRateRepository extends PagingAndSortingRepository<ProductIGenRate, String> {
    ProductIGenRate findByGender(String gender);
}
