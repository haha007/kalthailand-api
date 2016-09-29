package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

import java.util.Optional;

@Repository
public interface ProductPremiumRateRepository extends MongoRepository<ProductPremiumRate, String> {

    Optional<ProductPremiumRate> findOneByProductIdAndPackageNameAndGenderAndAge(String productId, String packageName, GenderCode gender, int age);

    Optional<ProductPremiumRate> findOneByProductIdAndGenderAndAge(String productId, GenderCode genderCode, int age);

    Optional<ProductPremiumRate> findOneByProductId(String productId);

    Optional<ProductPremiumRate> findOneByProductIdAndPackageName(String productId, String packageName);
}
