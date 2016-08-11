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
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
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
		
		for(int a=0;a<policies.size();a++){
			if(!StringUtil.isBlank(policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId())&&
					policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId().length()<PLAIN_TEXT_SIZE){
				policyPlainTextCount++;
			}
			int benefitSize = policies.get(a).getCoverages().size();
			for(int b=0;b<benefitSize;b++){
				if(!StringUtil.isBlank(policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId())&&
						policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId().length()<PLAIN_TEXT_SIZE){
					benefitPlainTextCount++;
				}
			}
			List<Payment> payments = paymentRepository.findByPolicyId(policies.get(a).getPolicyId());
			for(int c=0;c<payments.size();c++){
				if(!StringUtil.isBlank(payments.get(c).getRegistrationKey())&&
						payments.get(c).getRegistrationKey().length()<PLAIN_TEXT_SIZE){
					paymentPlainTextCount++;
				}
			}
		}
		
		encryptService.encryptThaiIdAndRegistrationKey();
		
		policies = null;
		policies = (List<Policy>) policyRepository.findAll();
		for(int a=0;a<policies.size();a++){
			if(!StringUtil.isBlank(policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId())&&
					policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId().length()>PLAIN_TEXT_SIZE){
				policyEncryptTextCount++;
			}
			int benefitSize = policies.get(a).getCoverages().size();
			for(int b=0;b<benefitSize;b++){
				if(!StringUtil.isBlank(policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId())&&
						policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId().length()>PLAIN_TEXT_SIZE){
					benefitEncryptTextCount++;
				}
			}
			List<Payment> payments = paymentRepository.findByPolicyId(policies.get(a).getPolicyId());
			for(int c=0;c<payments.size();c++){
				if(!StringUtil.isBlank(payments.get(c).getRegistrationKey())&&
						payments.get(c).getRegistrationKey().length()>PLAIN_TEXT_SIZE){
					paymentEncryptTextCount++;
				}
			}
		}
		
		if(policyPlainTextCount==0&&benefitPlainTextCount==0&&paymentPlainTextCount==0){
			policyPlainTextCount = policyEncryptTextCount;
			benefitPlainTextCount = benefitEncryptTextCount;
			paymentPlainTextCount = paymentEncryptTextCount;
		}
		
		assertThat(policyPlainTextCount).isEqualTo(policyEncryptTextCount);
		assertThat(benefitPlainTextCount).isEqualTo(benefitEncryptTextCount);
		assertThat(paymentPlainTextCount).isEqualTo(paymentEncryptTextCount);
		
	}

}
 