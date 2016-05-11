package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.products.ProductISafeRate;

@Repository
public interface ProductISafeRateRepository extends PagingAndSortingRepository<ProductISafeRate, String> {
    ProductISafeRate findByGender(String gender);
}
