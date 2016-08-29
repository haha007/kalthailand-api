package th.co.krungthaiaxa.api.elife.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentRepositoryTest {
    Logger logger = LoggerFactory.getLogger(PaymentRepositoryTest.class);

    @Inject
    private PaymentRepository paymentRepository;

    @Test
    public void can_query_by_policyId_and_status_and_dueDate() {
        String policyId = "507-4876896";
        PaymentStatus paymentStatus = PaymentStatus.NOT_PROCESSED;
        LocalDate searchToDueDate = LocalDate.now();
        LocalDate searchFromDueDate = searchToDueDate.minusMonths(3);
        Optional<Payment> paymentOptional = paymentRepository.findOneByPolicyIdAndDueDateRangeAndInStatus(policyId, searchFromDueDate, searchToDueDate, paymentStatus);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            logger.debug(ObjectMapperUtil.toStringMultiLine(payment));
            Assert.assertEquals(policyId, payment.getPolicyId());
            Assert.assertTrue(paymentStatus.equals(payment.getStatus()));
            String dueDateRangeErrorMessage = String.format("DueDate must be from %s to %s: %s", searchFromDueDate, searchToDueDate, ObjectMapperUtil.toString(payment));
            Assert.assertTrue(dueDateRangeErrorMessage, !payment.getDueDate().isBefore(searchFromDueDate));
            Assert.assertTrue(dueDateRangeErrorMessage, !payment.getDueDate().isAfter(searchToDueDate));
        }
    }
}
