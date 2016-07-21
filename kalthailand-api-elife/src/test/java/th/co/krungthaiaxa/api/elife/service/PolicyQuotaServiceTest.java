package th.co.krungthaiaxa.api.elife.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyQuotaServiceTest {
	
	@Inject
    private PolicyQuotaService policyQuotaService;
	
	@Test
	public void should_get_policy_quota_data(){
		PolicyQuota policyQuota = policyQuotaService.getPolicyQuota();
		assertThat(policyQuota).isNotNull();
	}

}
