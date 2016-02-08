package th.co.krungthaiaxa.ebiz.api.service;


import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.ebiz.api.KalApiApplication;
import th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.ebiz.api.model.Payment;
import th.co.krungthaiaxa.ebiz.api.model.Policy;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.repository.PaymentRepository;
import th.co.krungthaiaxa.ebiz.api.resource.TestUtil;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.emptyQuote;
import static th.co.krungthaiaxa.ebiz.api.exception.PolicyValidationException.noneExistingQuote;
import static th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.ebiz.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.ebiz.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.ebiz.api.resource.TestUtil.payment;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class PolicyServiceTest {

    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PaymentRepository paymentRepository;

    @Test
    public void should_return_error_when_create_policy_if_quote_not_provided() throws Exception {
        assertThatThrownBy(() -> policyService.createPolicy(null))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(emptyQuote.getMessage());
    }

    @Test
    public void should_return_error_when_create_policy_if_quote_does_not_exist() throws Exception {
        Quote quote = new Quote();
        quote.setTechnicalId("123");
        assertThatThrownBy(() -> policyService.createPolicy(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noneExistingQuote.getMessage());
    }

    @Test
    public void should_add_generated_ids_when_saving_policy_for_first_time() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        TestUtil.quote(quote);
        quote = quoteService.updateQuote(quote);

        Policy policy = policyService.createPolicy(quote);
        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getTechnicalId()).isNotNull();
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        Payment updatedPayment = policyService.updatePayment(payment.getPaymentId(), 100.0, "THB", null, ERROR, LINE, null, null, "Error msg");

        assertThat(updatedPayment.getEffectiveDate()).isNull();
        assertThat(updatedPayment.getPaymentInformations()).hasSize(1);
        assertThat(updatedPayment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Error msg");
        assertThat(updatedPayment.getStatus()).isEqualTo(INCOMLETE);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_amount_with_success_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 100.0, "THB", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_success_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(2);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_right_sum_with_minuses_and_success_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(payment.getPaymentId(), 75.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(payment.getPaymentId(), -25.0, "THB", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_completed_if_three_payments_of_right_amount_with_success_and_error_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, ERROR, LINE, null, null, null);
        policyService.updatePayment(payment.getPaymentId(), 50.0, "THB", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(3);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void should_mark_payment_as_incomplete_if_currency_different() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 100.0, "EUR", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Currencies are different");
        assertThat(payment.getStatus()).isEqualTo(INCOMLETE);
    }

    @Test
    public void should_mark_payment_as_overpaid_if_sum_of_success_payment_is_over_expected_amount() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(payment.getPaymentId(), 150.0, "THB", null, SUCCESS, LINE, null, null, null);

        payment = paymentRepository.findOne(payment.getPaymentId());
        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(OVERPAID);
    }

}
