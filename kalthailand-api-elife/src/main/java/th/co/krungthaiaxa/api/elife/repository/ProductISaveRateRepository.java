package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.ProductISaveRate;

@Repository
public interface ProductISaveRateRepository extends PagingAndSortingRepository<ProductISaveRate, String> {
    ProductISaveRate findByGender(String gender);
}
