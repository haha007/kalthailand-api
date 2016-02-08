package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.model.Payment;

@Repository
public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
}
