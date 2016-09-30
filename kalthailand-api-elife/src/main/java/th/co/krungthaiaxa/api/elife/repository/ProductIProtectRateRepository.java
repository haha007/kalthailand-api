package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.IProtectRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

import java.util.Optional;

@Repository
public interface ProductIProtectRateRepository extends MongoRepository<IProtectRate, String> {

    Optional<IProtectRate> findByPackageNameAndGenderAndAge(String packageName, GenderCode gender, int age);
}
