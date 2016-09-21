package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

import javax.inject.Inject;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class PolicyFactory {
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private QuoteService quoteService;

    @Inject
    private PolicyService policyService;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    public Policy createPolicyForLineWithPendingValidation(int age, String email) {
        Quote quote = quoteFactory.createDefaultiProtectQuoteForLine(age, email);
        return policyService.createPolicy(quote);
    }

    public Policy createPolicyForLineWithValidated(int age, String email) {
        Policy policy = createPolicyForLineWithPendingValidation(age, email);
        Payment payment = policy.getPayments().get(0);
        String orderId = RandomStringUtils.randomNumeric(10);
        String transactionId = RandomStringUtils.randomNumeric(15);
        String regKey = RandomStringUtils.randomNumeric(20);
        policyService.updatePayment(payment, orderId, transactionId, regKey);
        policyService.updatePolicyAfterFirstPaymentValidated(policy);
        policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "999999-99-999999", "agentName", "token");
        return policy;
    }
}
