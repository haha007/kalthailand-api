package th.co.krungthaiaxa.api.elife.service;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.repository.LineTokenRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyQuotaRepository;

@Service
public class PolicyQuotaService {
	
	private final static Logger logger = LoggerFactory.getLogger(PolicyQuotaService.class);
	private final PolicyQuotaRepository policyQuotaRepository;
	private final int POLICY_QUOTA_ROW_ID = 1;
	
	@Inject
	public PolicyQuotaService(PolicyQuotaRepository policyQuotaRepository){
		this.policyQuotaRepository = policyQuotaRepository;
	}
	
	public PolicyQuota getPolicyQuota(){
		logger.info(String.format("On %1$s .....", "getPolicyQuota"));
		PolicyQuota policyQuota = policyQuotaRepository.findByRowId(POLICY_QUOTA_ROW_ID);
		return policyQuota;
	}
	
	public void updatePolicyQuota(PolicyQuota updatePolicyQuota){
		logger.info(String.format("On %1$s .....", "updatePolicyQuota"));
		policyQuotaRepository.deleteAll();
		updatePolicyQuota.setRowId(POLICY_QUOTA_ROW_ID);
		policyQuotaRepository.save(updatePolicyQuota);
	}

}
