package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectDiscountRate;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectPackage;

import java.util.Optional;

@Repository
public interface ProductIProtectDiscountRateRepository extends PagingAndSortingRepository<IProtectDiscountRate, String> {

    @Query("{'packageName': ?0, 'sumInsured': {'$lte': ?1}}")
    Optional<IProtectDiscountRate> findByPackageNameAndSumInsuredLessThan(IProtectPackage packageName, double sumInsured, Pageable pageable);
}
