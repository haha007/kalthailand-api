package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectDiscountRateRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Service
public class IProtectDiscountRateService {
    private final ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository;

    @Inject
    public IProtectDiscountRateService(ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository) {this.productIProtectDiscountRateRepository = productIProtectDiscountRateRepository;}

    public Optional<IProtectDiscountRate> findIProtectDiscountRate(IProtectPackage iprotectPackage, double sumInsured) {
        Sort sort = new Sort(Sort.Direction.DESC, "sumInsured");
        PageRequest pageRequest = new PageRequest(0, 1, sort);
        List<IProtectDiscountRate> list = productIProtectDiscountRateRepository.findByPackageNameAndSumInsuredLessThan(iprotectPackage, sumInsured, pageRequest);
        return toOptionalOfFirstElement(list);
    }

    public IProtectDiscountRate validateExistHighestDiscountRate(IProtectPackage iProtectPackage) {
        Optional<IProtectDiscountRate> iProtectDiscountRateOptional = findExistHighestDiscountRate(iProtectPackage);
        return iProtectDiscountRateOptional.orElseThrow(() -> QuoteCalculationException.discountRateNotFound.apply("Not found highest discount rate!"));
    }

    public Optional<IProtectDiscountRate> findExistHighestDiscountRate(IProtectPackage iProtectPackage) {
        Sort sort = new Sort(Sort.Direction.DESC, "discountRate");
        PageRequest pageRequest = new PageRequest(0, 1, sort);
        List<IProtectDiscountRate> list = productIProtectDiscountRateRepository.findByPackageName(iProtectPackage, pageRequest);
        return toOptionalOfFirstElement(list);
    }

    private <T> Optional<T> toOptionalOfFirstElement(List<T> list) {
        T item;
        if (list.isEmpty()) {
            item = null;
        } else {
            item = list.get(0);
        }
        return Optional.ofNullable(item);
    }
}
