package th.co.krungthaiaxa.api.elife.commission.repositories;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionResult;

@Repository
public interface CommissionResultRepository extends MongoRepository<CommissionResult, String> {
	
	List<CommissionResult> findAllByOrderByCreatedDateTimeAsc();
	CommissionResult findByRowId(String rowId);

}
