package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.data.CampaignKTC;

@Repository
public interface CampaignKTCRepository extends MongoRepository<CampaignKTC, String> {
}
