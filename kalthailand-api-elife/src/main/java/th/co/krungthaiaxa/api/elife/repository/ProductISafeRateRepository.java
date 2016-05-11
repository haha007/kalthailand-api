package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.ProductISafeRate;

@Repository
public interface ProductISafeRateRepository extends PagingAndSortingRepository<ProductISafeRate, String> {
    ProductISafeRate findByGender(String gender);
}
