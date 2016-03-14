package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.products.ProductIFineRate;

@Repository
public interface ProductIFineRateRepository extends PagingAndSortingRepository<ProductIFineRate, String> {

    ProductIFineRate findByPlanNameAndGender(String packageName, String gender);
}
