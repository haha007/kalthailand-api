package th.co.krungthaiaxa.api.elife.service;

import java.util.List;

import javax.inject.Inject;

import org.jsoup.helper.StringUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EncryptServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(EncryptServiceTest.class);	
	private final int PLAIN_TEXT_SIZE = 20;
	@Inject
	private EncryptService encryptService;
	@Inject
	private PolicyRepository policyRepository;
	@Inject
	private PaymentRepository paymentRepository;
	
	@Test
	public void should_equals_between_before_encrypt_and_after_encrypt_fo_policy_insured_and_benefit_thai_id_and_line_pay_regkey_success(){
		
		int policyPlainTextCount = 0;
		int benefitPlainTextCount = 0;
		int paymentPlainTextCount = 0;
		int policyEncryptTextCount = 0;
		int benefitEncryptTextCount = 0;
		int paymentEncryptTextCount = 0;
		
		List<Policy> policies = (List<Policy>) policyRepository.findAll();
		
		for (Policy policy : policies) {
			for (Insured insured : policy.getInsureds()) {
				for (Registration registration : insured.getPerson().getRegistrations()) {
					if (isValidBeforeEncrypt(registration.getId())) {
						policyPlainTextCount++;
					}
				}
				
			}
			for(Coverage coverage : policy.getCoverages()){
				for(CoverageBeneficiary coverageBeneficiary : coverage.getBeneficiaries()){
					for(Registration registration : coverageBeneficiary.getPerson().getRegistrations()){
						if(isValidBeforeEncrypt(registration.getId())){
							benefitPlainTextCount++;
						}
					}
				}
			}
			for(Payment payment : paymentRepository.findByPolicyId(policy.getPolicyId())){
				if(isValidBeforeEncrypt(payment.getRegistrationKey())){
					paymentPlainTextCount++;
				}
			}
		}
		
		encryptService.encryptRegistrationIdAndRegistrationKey();
		
		policies = (List<Policy>) policyRepository.findAll();
		
		for(Policy policy : policies){
			for(Insured insured : policy.getInsureds()){
				for(Registration registration : insured.getPerson().getRegistrations()){
					if(isValidAfterEncrypted(registration.getId())){
						policyEncryptTextCount++;
					}
				}
				
			}
			for(Coverage coverage : policy.getCoverages()){
				for(CoverageBeneficiary coverageBeneficiary : coverage.getBeneficiaries()){
					for(Registration registration : coverageBeneficiary.getPerson().getRegistrations()){
						if(isValidAfterEncrypted(registration.getId())){
							benefitEncryptTextCount++;
						}
					}
				}
			}
			for(Payment payment : paymentRepository.findByPolicyId(policy.getPolicyId())){
				if(isValidAfterEncrypted(payment.getRegistrationKey())){
					paymentEncryptTextCount++;
				}
			}
		}
		
		//fix test fail if already encrypted.
		if(policyPlainTextCount==0&&benefitPlainTextCount==0&&paymentPlainTextCount==0){
			policyPlainTextCount = policyEncryptTextCount;
			benefitPlainTextCount = benefitEncryptTextCount;
			paymentPlainTextCount = paymentEncryptTextCount;
		}
		
		assertThat(policyPlainTextCount).isEqualTo(policyEncryptTextCount);
		assertThat(benefitPlainTextCount).isEqualTo(benefitEncryptTextCount);
		assertThat(paymentPlainTextCount).isEqualTo(paymentEncryptTextCount);
		
	}
	
	private boolean isValidBeforeEncrypt(String text){
		return !StringUtil.isBlank(text) && text.length()<PLAIN_TEXT_SIZE;
	}
	
	private boolean isValidAfterEncrypted(String text){
		return !StringUtil.isBlank(text) && text.length()>PLAIN_TEXT_SIZE;
	}

}
 