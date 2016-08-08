package th.co.krungthaiaxa.api.elife.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

import javax.inject.Inject;

import java.time.Instant;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyServiceTest extends ELifeTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

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
        quote1 = quoteService.updateQuote(quote1, "token");

        Quote quote2 = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation());
        TestUtil.quote(quote2, TestUtil.beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2, "token");
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
        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
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

        policyService.updatePayment(payment1, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment1, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment2, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment3, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        Assertions.assertThat(policy.getDocuments()).hasSize(0);
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("Error code", "Error msg"));
        policy.getPayments().get(0);

        assertThat(payment.getEffectiveDate()).isNull();
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

        policyService.updatePayment(payment, 100.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
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

        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
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

        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment, 75.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment, -25.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("OK", "OK", "OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_three_payments_of_right_amount_with_success_and_error_status() {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);

        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));
        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("someErrorCode", "someErrorMessage"));
        policyService.updatePayment(payment, 50.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
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

        policyService.updatePayment(payment, 100.0, "EUR", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isNull();
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

        policyService.updatePayment(payment, 150.0, "THB", ChannelType.LINE, TestUtil.linePayResponse("0000", "OK"));

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("OK");
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.OVERPAID);
    }

    @Test
    public void should_update_policy_status_to_pending_validation_and_attach_2_documents() {
        Policy policy = getPolicy();

        policyService.updatePolicyAfterFirstPaymentValidated(policy);

        Assertions.assertThat(policy.getStatus()).isEqualTo(PolicyStatus.PENDING_VALIDATION);
        Assertions.assertThat(policy.getDocuments()).hasSize(2);
    }

    @Test
    public void should_send_one_email_when_policy_status_is_set_to_pending_validation() {
        Policy policy = getPolicy();

        policyService.updatePolicyAfterFirstPaymentValidated(policy);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }

    @Test
    public void should_update_policy_status_to_validated_and_attach_5_documents() {
        Instant beforeValidate = Instant.now();

        Policy policy = getPolicy();
        policyService.updatePolicyAfterFirstPaymentValidated(policy);
        policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "999999-99-999999", "agentName", "token");

        Policy policyAfterUpdate = policyService.findPolicy(policy.getPolicyId()).get();
        Assertions.assertThat(policyAfterUpdate.getStatus()).isEqualTo(PolicyStatus.VALIDATED);
        Assertions.assertThat(policy.getDocuments()).hasSize(5);

        Instant afterValidate = Instant.now();
        Instant validationTime = policyAfterUpdate.getValidationDateTime();
        Assert.assertTrue(validationTime.isAfter(beforeValidate) && validationTime.isBefore(afterValidate));
    }

    @Test
    public void should_send_two_emails_when_policy_status_is_set_to_validated() {
        Policy policy = getPolicy();

        policyService.updatePolicyAfterFirstPaymentValidated(policy);
        policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "999999-99-999999", "agentName", "token");

        assertThat(greenMail.getReceivedMessages()).hasSize(2);
    }

    @Test
    public void should_not_update_policy_status_to_validated_when_previous_status_is_not_pending_validation() {
        Policy policy = getPolicy();

        assertThatThrownBy(() -> policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "999999-99-999999", "agentName", "token"))
                .isInstanceOf(ElifeException.class);
        Assertions.assertThat(policy.getDocuments()).hasSize(0);
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation(25, PeriodicityCode.EVERY_MONTH));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        return policyService.createPolicy(quote);
    }

}
