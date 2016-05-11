package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.ProductIFineRate;

@Repository
public interface ProductIFineRateRepository extends PagingAndSortingRepository<ProductIFineRate, String> {

    ProductIFineRate findByPlanNameAndGender(String packageName, String gender);
}
