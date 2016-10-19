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
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyNumberServiceTest extends ELifeTest {
    public static final Logger logger = LoggerFactory.getLogger(PolicyNumberServiceTest.class);

    @Inject
    private PolicyNumberService policyNumberService;

    @Test
    public void can_count_available_policies() {
        long quota = policyNumberService.countAllPolicyNumbers();
        long availablePolicies = policyNumberService.countAvailablePolicyNumbers();
        logger.info(" Quota: {}.%n Remain policies: {}", quota, availablePolicies, availablePolicies);
        Assert.assertTrue(availablePolicies <= quota);
    }

}
