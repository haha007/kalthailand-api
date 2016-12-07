package th.co.krungthaiaxa.api.elife.commission.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;

import java.util.List;

@Repository
public interface CommissionCalculationSessionRepository extends MongoRepository<CommissionCalculationSession, String> {

    List<CommissionCalculationSession> findAllByOrderByCreatedDateTimeAsc();
}
