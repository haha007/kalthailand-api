package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import th.co.krungthaiaxa.elife.api.products.ProductIBeginRate;

public interface ProductIBeginRateRepository extends PagingAndSortingRepository<ProductIBeginRate, String> {
    @Query("{ 'nbOfYearsOfPayment' : ?0, sumInsured : ?1 }")
    ProductIBeginRate findByNbOfYearsOfPaymentAndSumInsured(Integer nbOfYearsOfPayment, Double sumInsured);
}
