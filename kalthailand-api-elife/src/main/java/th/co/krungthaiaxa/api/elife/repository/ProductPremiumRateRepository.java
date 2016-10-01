package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPremiumRateRepository extends MongoRepository<ProductPremiumRate, String> {
    List<ProductPremiumRate> findByProductId(String productId);

    List<ProductPremiumRate> findByProductIdAndPackageName(String productId, String packageName);

    //BY PRODUCT_ID ///////////////////////////////////

    Optional<ProductPremiumRate> findOneByProductId(String productId);

    Optional<ProductPremiumRate> findOneByProductIdAndGender(String productId, GenderCode genderCode);

    Optional<ProductPremiumRate> findOneByProductIdAndAge(String productId, int age);

    Optional<ProductPremiumRate> findOneByProductIdAndGenderAndAge(String productId, GenderCode genderCode, int age);

    //BY PRODUCT_ID AND PACKAGE_NAME ///////////////////////////////////

    Optional<ProductPremiumRate> findOneByProductIdAndPackageName(String productId, String packageName);

    Optional<ProductPremiumRate> findOneByProductIdAndPackageNameAndAge(String productId, String packageName, int age);

    Optional<ProductPremiumRate> findOneByProductIdAndPackageNameAndGender(String productId, String packageName, GenderCode genderCode);

    Optional<ProductPremiumRate> findOneByProductIdAndPackageNameAndGenderAndAge(String productId, String packageName, GenderCode gender, int age);

}
