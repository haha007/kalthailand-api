package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectDiscountRate;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectPackage;

import java.util.List;

@Repository
public interface ProductIProtectDiscountRateRepository extends MongoRepository<IProtectDiscountRate, String> {

    @Query("{'packageName': ?0, 'sumInsured': {'$lte': ?1}}")
    List<IProtectDiscountRate> findByPackageNameAndSumInsuredLessThan(IProtectPackage packageName, double sumInsured, Pageable pageable);
}
