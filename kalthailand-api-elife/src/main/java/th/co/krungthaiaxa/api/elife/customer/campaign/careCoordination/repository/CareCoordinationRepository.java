package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.data.CareCoordinationEntity;

/**
 * @author tuong.le on 10/31/17.
 */
public interface CareCoordinationRepository extends MongoRepository<CareCoordinationEntity, String> {
}
