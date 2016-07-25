package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.PolicyNumberSetting;

import java.util.stream.Stream;

@Repository
public interface PolicyNumberSettingRepository extends MongoRepository<PolicyNumberSetting, String> {

}
