package th.co.krungthaiaxa.api.elife.test.service;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.anyString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.service.PaymentService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentServiceTest {
    Logger logger = LoggerFactory.getLogger(PaymentServiceTest.class);

    @Inject
    private PaymentService paymentService;

    @Inject
    private PaymentRepository paymentRepository;
    
    private Payment createSamplePayment(String policyId, String regKey, LocalDateTime dueDate) {
        return createSamplePayment(policyId, regKey, PaymentStatus.NOT_PROCESSED, dueDate);
    }
    
    
    private Payment createSamplePayment(String policyId, String regKey, PaymentStatus status, LocalDateTime dueDate){
    	Payment payment = new Payment();
        payment.setPolicyId(policyId);
        payment.setRegistrationKey(regKey);
        payment.setStatus(status);
        payment.setDueDate(dueDate);
        return paymentRepository.save(payment);
    }

    @Test
    public void can_query_by_policyId_and_and_regKeyNotNull() {
        String testingPolicyId = "000-000000A";
        LocalDateTime payment2DueDate = LocalDateTime.now();
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
    
	@Test
	public void shouldFindLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull_WhenThereAreMultiplePaymentRecordsOnSameDueDate() {
		List<Payment> payments = new ArrayList<>();

		String testingPolicyId = "000-000000A";
		LocalDateTime payment2DueDate = LocalDateTime.now();
		payments.add(createSamplePayment(testingPolicyId, "regKey1", PaymentStatus.INCOMPLETE, payment2DueDate));
		payments.add(createSamplePayment(testingPolicyId, "regKey2", PaymentStatus.COMPLETED, payment2DueDate));
		payments.add(
				createSamplePayment(testingPolicyId, "regKey3", PaymentStatus.COMPLETED, payment2DueDate.minusDays(1)));
		payments.add(
				createSamplePayment(testingPolicyId, null, PaymentStatus.NOT_PROCESSED, payment2DueDate.plusDays(1)));

		Optional<Payment> paymentOptional = paymentService
				.findLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull(testingPolicyId);
		logger.debug(ObjectMapperUtil.toStringMultiLine(paymentOptional.get()));
		paymentRepository.delete(payments);
		
		Assert.assertEquals(PaymentStatus.COMPLETED, paymentOptional.get().getStatus());
		Assert.assertEquals("regKey2", paymentOptional.get().getRegistrationKey());
	}
	
	@Test
	public void shouldGetNull_WhenFindLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull_ThenNull(){
		PaymentService fakePaymentService = Mockito.spy(paymentService);
		doReturn(null).when(fakePaymentService).findLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull(anyString());
		
		// assert
		Assert.assertEquals(null, paymentService.findLastRegistrationKey("12345"));
	}
	
	
	@Test
	public void shouldGetRegistrationKey_WhenFindLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull_ThenNotNull() {
		Payment payment = new Payment();
		payment.setRegistrationKey("regKey1234");
		Optional<Payment> paymentOptional = Optional.of(payment);
		
		PaymentService fakePaymentService = Mockito.spy(paymentService);
		doReturn(paymentOptional).when(fakePaymentService).findLastestCompletedPaymentByPolicyNumberAndRegKeyNotNull(anyString());
		
		// assert
		Assert.assertEquals("regKey1234", fakePaymentService.findLastRegistrationKey("12345"));
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
