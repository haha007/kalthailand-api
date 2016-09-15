package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;

@Repository
public interface GeneralSettingRepository extends MongoRepository<GeneralSetting, String> {

}
