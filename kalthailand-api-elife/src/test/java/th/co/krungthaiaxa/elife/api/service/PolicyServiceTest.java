package th.co.krungthaiaxa.elife.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.emptyQuote;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.noneExistingQuote;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyServiceTest {
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    @Test
    public void should_return_error_when_create_policy_if_quote_not_provided() throws Exception {
        assertThatThrownBy(() -> policyService.createPolicy(null))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(emptyQuote.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_if_quote_does_not_exist() throws Exception {
        Quote quote = new Quote();
        quote.setId("123");
        assertThatThrownBy(() -> policyService.createPolicy(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noneExistingQuote.getMessage());
    }

    @Test
    public void should_update_quote_with_policy_id_when_policy_has_been_created() throws QuoteCalculationException, PolicyValidationException {
        String sessionId = randomNumeric(20);

        Quote quote1 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote1, beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);

        Quote quote2 = quoteService.createQuote(sessionId, LINE, productQuotation());
        quote(quote2, beneficiary(100.0));
        quote2 = quoteService.updateQuote(quote2);
        Policy policy = policyService.createPolicy(quote2);

        assertThat(quoteService.findByQuoteId(quote1.getQuoteId(), sessionId, LINE).get().getPolicyId()).isNull();
        assertThat(quoteService.findByQuoteId(quote2.getQuoteId(), sessionId, LINE).get().getPolicyId()).isEqualTo(policy.getPolicyId());
    }

    @Test
    public void should_add_generated_ids_when_saving_policy_for_first_time() throws QuoteCalculationException, PolicyValidationException {
        Policy policy = getPolicy();
        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getId()).isNotNull();
    }

    @Test
    public void should_have_0_documents_after_updating_first_payment() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        assertThat(policy.getDocuments()).hasSize(0);
    }

    @Test
    public void should_still_have_only_0_documents_even_after_updating_multiple_payments() throws Exception {
        Policy policy = getPolicy();

        Payment payment1 = policy.getPayments().get(0);
        Payment payment2 = policy.getPayments().get(1);
        Payment payment3 = policy.getPayments().get(2);

        policyService.updatePayment(policy, payment1, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment1, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment2, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment3, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(policy.getDocuments()).hasSize(0);
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 100.0, "THB", empty(), ERROR, LINE, empty(), empty(), empty(), Optional.of("Error msg"));
        policy.getPayments().get(0);

        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Error msg");
        assertThat(payment.getStatus()).isEqualTo(INCOMPLETE);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_amount_with_success_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 100.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_success_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(2);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_minuses_and_success_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment, 75.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment, -25.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_three_payments_of_right_amount_with_success_and_error_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), ERROR, LINE, empty(), empty(), empty(), empty());
        policyService.updatePayment(policy, payment, 50.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_incomplete_if_currency_different() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 100.0, "EUR", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Currencies are different");
        assertThat(payment.getStatus()).isEqualTo(INCOMPLETE);
    }

    @Test
    public void should_mark_payment_as_overpaid_if_sum_of_success_payment_is_over_expected_amount() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 150.0, "THB", empty(), SUCCESS, LINE, empty(), empty(), empty(), empty());

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(OVERPAID);
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }

}
