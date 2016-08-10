package th.co.krungthaiaxa.api.elife.service;

import java.util.List;

import javax.inject.Inject;

import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.text.pdf.StringUtils;

import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

@Service
public class EncryptService {
	
	private final static Logger logger = LoggerFactory.getLogger(EncryptService.class);	
	private final PolicyRepository policyRepository;
	private final PaymentRepository paymentRepository;
	//must be compatible between thai id (13) and regkey (?)
	private final int PLAIN_TEXT_SIZE = 20;
	
	@Inject
	public EncryptService(PolicyRepository policyRepository, PaymentRepository paymentRepository){
		this.policyRepository = policyRepository;
		this.paymentRepository = paymentRepository;
	}
	
	public void encryptData(){
		List<Policy> policies  = (List<Policy>) policyRepository.findAll();		
		for(int a=0;a<policies.size();a++){
			if(!StringUtil.isBlank(policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId())&&
					policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId().length()<PLAIN_TEXT_SIZE){
				policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).setId(EncryptUtil.encrypt(policies.get(a).getInsureds().get(0).getPerson().getRegistrations().get(0).getId()));
			}
			int benefitSize = policies.get(a).getCoverages().size();
			for(int b=0;b<benefitSize;b++){
				if(!StringUtil.isBlank(policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId())&&
						policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId().length()<PLAIN_TEXT_SIZE){
					policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).setId(EncryptUtil.encrypt(policies.get(a).getCoverages().get(0).getBeneficiaries().get(b).getPerson().getRegistrations().get(0).getId()));
				}
			}
			List<Payment> payments = paymentRepository.findByPolicyId(policies.get(a).getPolicyId());
			for(int c=0;c<payments.size();c++){
				if(!StringUtil.isBlank(payments.get(c).getRegistrationKey())&&
						payments.get(c).getRegistrationKey().length()<PLAIN_TEXT_SIZE){
					payments.get(c).setRegistrationKey(EncryptUtil.encrypt(payments.get(c).getRegistrationKey()));
				}
			}
			paymentRepository.save(payments);
		}
		policyRepository.save(policies);
	}
	
}
