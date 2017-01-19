package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;

@Repository
public interface CustomerAnniversary20Repository extends MongoRepository<CustomerAnniversary20, String> {
}
