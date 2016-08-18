package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectPackage;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectRate;

import java.util.Optional;

@Repository
public interface ProductIProtectRateRepository extends PagingAndSortingRepository<IProtectRate, String> {

    Optional<IProtectRate> findByPackageNameAndGenderAndAge(IProtectPackage packageName, GenderCode gender, int age);
}
