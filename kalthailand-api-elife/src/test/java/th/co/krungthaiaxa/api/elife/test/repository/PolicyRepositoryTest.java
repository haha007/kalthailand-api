package th.co.krungthaiaxa.api.elife.test.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

/**
 * @author tuong.le on 11/30/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyRepositoryTest {
    
    @Inject
    private PolicyRepository policyRepository;
    
    @Test
    public void should_get_all_policies_for_remind(){
        List<Policy> policies = policyRepository.findAllPolicyByStatusOnDate(PolicyStatus.PENDING_PAYMENT, LocalDate.now().minusDays(2));
        Assert.notNull(policies, "Could not found any policy on 2 days back");
    }
}
