package th.co.krungthaiaxa.api.elife.service;

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
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentServiceTest {
    Logger logger = LoggerFactory.getLogger(PaymentServiceTest.class);

    @Inject
    private PaymentService paymentService;

    @Inject
    private PaymentRepository paymentRepository;

    @Test
    public void can_query_by_policyId_and_regKeyNotNull() {
        String testingPolicyId = "000-000000A";
        LocalDate payment2DueDate = LocalDate.now();
        List<Payment> payments = new ArrayList<>();
        payments.add(createSamplePayment(testingPolicyId, "regKey1", payment2DueDate.minusDays(1)));
        payments.add(createSamplePayment(testingPolicyId, "regKey2", payment2DueDate));
        payments.add(createSamplePayment(testingPolicyId, "regKey3", payment2DueDate.minusDays(2)));
        payments.add(createSamplePayment(testingPolicyId, null, payment2DueDate.plusDays(1)));
        payments.add(createSamplePayment(testingPolicyId, null, payment2DueDate.minusDays(1)));
        Optional<Payment> paymentOptional = paymentService.findLastestPaymentByPolicyNumberAndRegKeyNotNull(testingPolicyId);
        logger.debug(ObjectMapperUtil.toStringMultiLine(paymentOptional.get()));
        paymentRepository.delete(payments);
        Assert.assertEquals("regKey2", paymentOptional.get().getRegistrationKey());
    }

    private Payment createSamplePayment(String policyId, String regKey, LocalDate dueDate) {
        Payment payment = new Payment();
        payment.setPolicyId(policyId);
        payment.setRegistrationKey(regKey);
        payment.setDueDate(dueDate);
        return paymentRepository.save(payment);
    }

    @Test
    public void can_get_null_value() {
        List<String> testingRegistrationKeys = Arrays.asList(null, "", " ", "aaa");
        for (String testingRegistrationKey : testingRegistrationKeys) {
            Payment payment = new Payment();
            payment.setRegistrationKey(testingRegistrationKey);
            String resultRegistrationKey = payment.getRegistrationKey();

            if (testingRegistrationKey == null) {
                Assert.assertNull(resultRegistrationKey);
            } else {
                Assert.assertEquals(testingRegistrationKey, resultRegistrationKey);
            }
        }

    }

}
