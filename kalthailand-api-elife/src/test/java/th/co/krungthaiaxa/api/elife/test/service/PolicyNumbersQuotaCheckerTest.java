package th.co.krungthaiaxa.api.elife.test.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.service.PolicyNumbersQuotaCheckerService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * @author khoi.tran on 7/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyNumbersQuotaCheckerTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyNumbersQuotaCheckerService policyNumbersQuotaCheckerService;

    @Before
    public void setup() {
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    /**
     * @implNote When trying to test, you may want to create more policy to reach the quota, then you can use this method.
     * Before running this, you must have public.key & private.key on your folder "/opt/keys".
     * If not exist, you can generate them by using RSAUtil.generateKeys()
     */
//    @Test
    public void create_sample_policies() {
        createSamplePolicies(10);
    }

    /**
     * @implNote in order to run this test, you have to start your local email server or use greenmail & mongoDB server.
     */
    @Test
    public void policy_number_quota_checker_should_work_normally() {
        PolicyNumbersQuotaCheckerService.PolicyNumbersQuotaCheckerResult checkerResult = policyNumbersQuotaCheckerService.checkEnoughRemainPolicyNumbers();
        if (checkerResult != null) {
            long totalPolicyNumbers = checkerResult.getTotalPolicyNumbers();
            long availablePolicyNumbers = checkerResult.getAvailablePolicyNumbers();
            long usedPolicyNumbers = totalPolicyNumbers - availablePolicyNumbers;

            double usedPercent = ((double) usedPolicyNumbers / totalPolicyNumbers) * 100;
            int triggerPercent = checkerResult.getPolicyNumberSetting().getTriggerPercent();
            boolean expectResultOfCheckingOverQuota = Math.round(usedPercent) >= triggerPercent;

            boolean actualResultOfCheckingOverQuota = checkerResult.isNearlyOverQuota();
            Assert.assertEquals(expectResultOfCheckingOverQuota, actualResultOfCheckingOverQuota);
        }
    }

    private void createSamplePolicies(int numberOfPolicies) {
        String sessionId = randomNumeric(20);
        for (int i = 0; i < (numberOfPolicies - 1); i++) {
            Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation());
            TestUtil.quote(quote, TestUtil.beneficiary(100.0));
            policyService.createPolicy(quote);
        }
    }
}
