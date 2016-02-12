package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.model.Payment;

@Repository
public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
}
