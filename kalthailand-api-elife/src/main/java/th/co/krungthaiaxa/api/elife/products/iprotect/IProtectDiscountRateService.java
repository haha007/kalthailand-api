package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectDiscountRateRepository;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class IProtectDiscountRateService {
    private final ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository;

    @Inject
    public IProtectDiscountRateService(ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository) {this.productIProtectDiscountRateRepository = productIProtectDiscountRateRepository;}

    public Optional<IProtectDiscountRate> findIProtectDiscountRate(IProtectPackage iprotectPackage, double sumInsured) {
        PageRequest pageRequest = new PageRequest(0, 1);
        return productIProtectDiscountRateRepository.findByPackageNameAndSumInsuredLessThan(iprotectPackage, sumInsured, pageRequest);
    }

}
