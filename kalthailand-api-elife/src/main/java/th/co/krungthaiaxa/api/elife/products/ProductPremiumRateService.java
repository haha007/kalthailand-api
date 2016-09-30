package th.co.krungthaiaxa.api.elife.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.repository.ProductPremiumRateRepository;

import java.util.Optional;

@Service
public class ProductPremiumRateService {
    private final ProductPremiumRateRepository productIProtectRateRepository;

    @Autowired
    public ProductPremiumRateService(ProductPremiumRateRepository productIProtectRateRepository) {this.productIProtectRateRepository = productIProtectRateRepository;}

//    public Optional<ProductPremiumRate> findPremiumRateByProductSpecId(ProductSpecId productSpecId) {
//
//    }

    public Optional<ProductPremiumRate> findPremiumRateByProductId(String productId) {
        return productIProtectRateRepository.findOneByProductId(productId);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageName(String productId, String packageName) {
        return productIProtectRateRepository.findOneByProductIdAndPackageName(productId, packageName);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndAgeAndGender(String productId, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findOneByProductIdAndGenderAndAge(productId, genderCode, age);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageNameAndAgeAndGender(String productId, String packageName, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findOneByProductIdAndPackageNameAndGenderAndAge(productId, packageName, genderCode, age);
    }

}
