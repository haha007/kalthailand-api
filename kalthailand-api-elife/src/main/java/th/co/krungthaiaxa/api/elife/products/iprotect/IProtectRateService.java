package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.IProtectRate;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectRateRepository;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class IProtectRateService {
    private final ProductIProtectRateRepository productIProtectRateRepository;

    @Inject
    public IProtectRateService(ProductIProtectRateRepository productIProtectRateRepository) {this.productIProtectRateRepository = productIProtectRateRepository;}

    public Optional<IProtectRate> findIProtectRates(String packageName, int age, GenderCode genderCode) {
        return productIProtectRateRepository.findByPackageNameAndGenderAndAge(packageName, genderCode, age);
    }


}
