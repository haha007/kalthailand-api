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

    public Optional<ProductPremiumRate> findPremiumRateByProductId(String productId) {
        return productIProtectRateRepository.findByProductId(productId);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageName(String productId, String packageName) {
        return productIProtectRateRepository.findByProductIdAndPackageName(productId, packageName);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndAgeAndGender(String productId, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findByProductIdAndGenderAndAge(productId, genderCode, age);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageNameAndAgeAndGender(String productId, String packageName, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findByProductIdAndPackageNameAndGenderAndAge(productId, packageName, genderCode, age);
    }

}
