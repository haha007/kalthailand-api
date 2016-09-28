package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

import java.util.Optional;

@Repository
public interface ProductPremiumRateRepository extends MongoRepository<ProductPremiumRate, String> {

    Optional<ProductPremiumRate> findByProductIdAndPackageNameAndGenderAndAge(String productId, String packageName, GenderCode gender, int age);

    Optional<ProductPremiumRate> findByProductIdAndGenderAndAge(String productId, GenderCode genderCode, int age);

    Optional<ProductPremiumRate> findByProductId(String productId);

    Optional<ProductPremiumRate> findByProductIdAndPackageName(String productId, String packageName);
}
