package th.co.krungthaiaxa.api.elife.test.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.service.PaymentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyServiceTest extends ELifeTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PaymentService paymentService;
    @Inject
    private PolicyFactory policyFactory;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void should_return_error_when_create_policy_if_quote_not_provided() {
        assertThatThrownBy(() -> policyService.createPolicy(null))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.emptyQuote.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_if_quote_does_not_exist() {
        Quote quote = new Quote();
        quote.setId("123");
        assertThatThrownBy(() -> policyService.createPolicy(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(PolicyValidationException.noneExistingQuote.getMessage());
    }

    @Test
    public void should_update_quote_with_policy_id_when_policy_has_been_created() {
        String sessionId = randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation());
        TestUtil.quote(quote1, TestUtil.beneficiary(100.0));
        quote1 = quoteService.updateProfessionNameAndCheckBlackList(quote1, "token");

        Quote quote2 = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation());
        TestUtil.quote(quote2, TestUtil.beneficiary(100.0));
        quote2 = quoteService.updateProfessionNameAndCheckBlackList(quote2, "token");
        Policy policy = policyService.createPolicy(quote2);

        assertThat(quoteService.findByQuoteId(quote1.getQuoteId(), sessionId, ChannelType.LINE).get().getPolicyId()).isNull();
        assertThat(quoteService.findByQuoteId(quote2.getQuoteId(), sessionId, ChannelType.LINE).get().getPolicyId()).isEqualTo(policy.getPolicyId());
    }

    @Test
    public void should_add_generated_ids_when_saving_policy_for_first_time() {
        Policy policy = getPolicy();
        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getId()).isNotNull();
    }

    @Test
    public void should_have_0_documents_after_updating_first_payment() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        Assertions.assertThat(policy.getDocuments()).hasSize(0);
    }

    @Test
    public void should_create_policy_with_default_pending_status() {
        Policy policy = getPolicy();

        Assertions.assertThat(policy.getStatus()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
    }

    @Test
    public void should_still_have_only_0_documents_even_after_updating_multiple_payments() {
        Policy policy = getPolicy();

        Payment payment1 = policy.getPayments().get(0);
        Payment payment2 = policy.getPayments().get(1);
        Payment payment3 = policy.getPayments().get(2);

        paymentService.updatePaymentAfterLinePay(payment1, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment1, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment2, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment3, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        Assertions.assertThat(policy.getDocuments()).hasSize(0);
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("Error code", "Error msg"));
        policy.getPayments().get(0);

//        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorCode").containsOnly("Error code");
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Error msg");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_amount_with_success_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 100.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertEffectiveDateSameDueDate(payment);
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsExactly("OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_success_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertEffectiveDateSameDueDate(payment);
        assertThat(payment.getPaymentInformations()).hasSize(2);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsExactly("OK", "OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_minuses_and_success_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment, 75.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment, -25.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertEffectiveDateSameDueDate(payment);
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("OK", "OK", "OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    private void assertEffectiveDateSameDueDate(Payment payment) {
        assertThat(payment.getEffectiveDate().toLocalDate()).isEqualTo(payment.getDueDate().toLocalDate());
    }

    @Test
    public void should_mark_payment_as_completed_if_three_payments_of_right_amount_with_success_and_error_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("someErrorCode", "someErrorMessage"));
        paymentService.updatePaymentAfterLinePay(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertEffectiveDateSameDueDate(payment);
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("OK", "someErrorMessage", "OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    public void should_mark_payment_as_incomplete_if_currency_different() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 100.0, "EUR", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

//        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Currencies are different");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
    }

    @Test
    public void should_mark_payment_as_overpaid_if_sum_of_success_payment_is_over_expected_amount() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        paymentService.updatePaymentAfterLinePay(payment, 150.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertEffectiveDateSameDueDate(payment);
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.OVERPAID);
    }

    @Test
    public void should_update_policy_status_to_pending_validation_and_attach_2_documents() {
        Policy policy = getPolicy();

        policyService.updatePolicyStatusToPendingValidation(policy);

        Assertions.assertThat(policy.getStatus()).isEqualTo(PolicyStatus.PENDING_VALIDATION);
        Assertions.assertThat(policy.getDocuments()).hasSize(2);
    }

    @Test
    public void should_send_one_email_when_policy_status_is_set_to_pending_validation() {
        Policy policy = getPolicy();

        policyService.updatePolicyStatusToPendingValidation(policy);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }

    @Test
    public void should_send_two_emails_when_policy_status_is_set_to_validated() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
        policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefault());
        assertThat(greenMail.getReceivedMessages()).hasSize(2);
    }

    @Test
    public void should_not_update_policy_status_to_validated_when_previous_status_is_not_pending_validation() {
        Policy policy = getPolicy();

        assertThatThrownBy(() -> policyService.updatePolicyStatusToValidated(policy, "999999-99-999999", "agentName", "token"))
                .isInstanceOf(ElifeException.class);
        Assertions.assertThat(policy.getDocuments()).hasSize(0);
    }

    private Policy getPolicy() {
        return policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment());
    }

}
