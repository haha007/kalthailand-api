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

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndGender(String productId, GenderCode genderCode) {
        return productIProtectRateRepository.findOneByProductIdAndGender(productId, genderCode);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndAge(String productId, int age) {
        return productIProtectRateRepository.findOneByProductIdAndAge(productId, age);

    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndGenderAndAge(String productId, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findOneByProductIdAndGenderAndAge(productId, genderCode, age);
    }

    //BY PRODUCT_ID AND PACKAGE_NAME
    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageName(String productId, String packageName) {
        return productIProtectRateRepository.findOneByProductIdAndPackageName(productId, packageName);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageNameAndGender(String productId, String packageName, GenderCode genderCode) {
        return productIProtectRateRepository.findOneByProductIdAndPackageNameAndGender(productId, packageName, genderCode);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageNameAndAge(String productId, String packageName, int age) {
        return productIProtectRateRepository.findOneByProductIdAndPackageNameAndAge(productId, packageName, age);
    }

    public Optional<ProductPremiumRate> findPremiumRateByProductIdAndPackageNameAndGenderAndAge(String productId, String packageName, GenderCode genderCode, int age) {
        return productIProtectRateRepository.findOneByProductIdAndPackageNameAndGenderAndAge(productId, packageName, genderCode, age);
    }
}
