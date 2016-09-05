package th.co.krungthaiaxa.api.elife.commission.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;

@Repository
public interface CommissionPlanRepository extends MongoRepository<CommissionPlan, ObjectId> {

}
