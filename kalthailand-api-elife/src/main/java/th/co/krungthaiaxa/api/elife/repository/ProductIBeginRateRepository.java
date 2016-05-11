package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.ProductIBeginRate;

@Repository
public interface ProductIBeginRateRepository extends PagingAndSortingRepository<ProductIBeginRate, String> {
    @Query("{ 'nbOfYearsOfPayment' : ?0, sumInsured : ?1 }")
    ProductIBeginRate findByNbOfYearsOfPaymentAndSumInsured(Integer nbOfYearsOfPayment, Double sumInsured);
}
