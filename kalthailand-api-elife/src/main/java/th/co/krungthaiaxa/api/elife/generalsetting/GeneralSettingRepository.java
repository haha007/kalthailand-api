package th.co.krungthaiaxa.api.elife.generalsetting;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSetting;

@Repository
public interface GeneralSettingRepository extends MongoRepository<GeneralSetting, String> {

}
