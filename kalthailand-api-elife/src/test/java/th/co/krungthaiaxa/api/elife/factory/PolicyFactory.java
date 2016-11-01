package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.service.PaymentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.PolicyValidatedProcessingService;

import javax.inject.Inject;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class PolicyFactory {
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private PolicyValidatedProcessingService policyValidatedProcessingService;

    @Inject
    private PolicyService policyService;
    @Inject
    private PaymentService paymentService;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    public Policy createPolicyForLineWithPendingValidation(int age, String email) {
        Quote quote = quoteFactory.createDefaultIProtectQuoteForLine(age, email);
        return policyService.createPolicy(quote);
    }

    public Policy createPolicyForLineWithValidated(int age, String email) {
        Quote quote = quoteFactory.createDefaultIProtectQuoteForLine(age, email);
        return createPolicyWithValidatedStatus(quote);
    }

    public Policy createPolicyWithValidatedStatus(ProductQuotation productQuotation) {
        return createPolicyWithValidatedStatus(productQuotation, TestUtil.DUMMY_EMAIL);
    }

    public Policy createPolicyWithPendingPaymentStatus(ProductQuotation productQuotation, String insuredEmail) {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createQuote(productQuotation, insuredEmail);
        return createPolicyWithPendingPaymentStatus(quoteResult.getQuote());
    }

    public Policy createPolicyWithValidatedStatus(ProductQuotation productQuotation, String insuredEmail) {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createQuote(productQuotation, insuredEmail);
        return createPolicyWithValidatedStatus(quoteResult.getQuote());
    }

    public Policy createPolicyWithPendingPaymentStatus(ProductQuotation productQuotation) {
        return createPolicyWithPendingPaymentStatus(productQuotation, TestUtil.DUMMY_EMAIL);
    }

    public Policy createPolicyWithPendingPaymentStatus(Quote quote) {
        return policyService.createPolicy(quote);
    }

    public Policy createPolicyWithPendingValidationStatus(ProductQuotation productQuotation) {
        Policy policy = createPolicyWithPendingPaymentStatus(productQuotation);
        return updateFromPendingPaymentToPendingValidation(policy);
    }

    public Policy createPolicyWithPendingValidationStatus(Quote quote) {
        Policy policy = createPolicyWithPendingPaymentStatus(quote);
        return updateFromPendingPaymentToPendingValidation(policy);
    }

    public Policy updateFromPendingPaymentToPendingValidation(Policy policy) {
        Payment payment = policy.getPayments().get(0);
        String orderId = PaymentFactory.generateOrderId();
        String transactionId = PaymentFactory.generateTransactionId();
        String regKey = PaymentFactory.generatePaymentRegKey();
        //Change status to PendingValidation
        paymentService.updatePayment(payment, orderId, transactionId, regKey);
        policyService.updatePolicyStatusToPendingValidation(policy);
        return policy;
    }

    public Policy createPolicyWithValidatedStatus(Quote quote) {
        Policy policy = createPolicyWithPendingValidationStatus(quote);

        //Change status to Validated
        PolicyValidatedProcessingService.PolicyValidationRequest policyValidationRequest = new PolicyValidatedProcessingService.PolicyValidationRequest();
        policyValidationRequest.setAccessToken(RequestFactory.generateAccessToken());
        policyValidationRequest.setAgentCode("123456-78-901234");
        policyValidationRequest.setAgentName("Mock Agent Name");
        policyValidationRequest.setLinePayCaptureMode(LinePayCaptureMode.FAKE_WITH_SUCCESS);
        policyValidationRequest.setPolicyId(policy.getPolicyId());
        policy = policyValidatedProcessingService.updatePolicyStatusToValidated(policyValidationRequest);
//        policy = policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "999999-99-999999", "Mock Agent Name", RequestFactory.generateAccessToken());
        return policy;
    }
}
