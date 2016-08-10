package th.co.krungthaiaxa.api.elife.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Payment;

@Repository
public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
	List<Payment> findByPolicyId(String policyId);
}
