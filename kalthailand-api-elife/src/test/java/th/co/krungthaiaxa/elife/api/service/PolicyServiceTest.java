package th.co.krungthaiaxa.elife.api.service;


import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.resource.TestUtil;
import th.co.krungthaiaxa.elife.api.utils.ImageUtil;

import javax.inject.Inject;
import java.io.File;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.emptyQuote;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.noneExistingQuote;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.elife.api.products.Product10EC.getCommonData;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
public class PolicyServiceTest {

    private final static String ERECEIPT_PDF_FILE_NAME= "ereceipt.pdf";
    @Value("${path.store.watermarked.image}")
    private String storePath;
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
        quote.setId("123");
        assertThatThrownBy(() -> policyService.createPolicy(quote))
                .isInstanceOf(PolicyValidationException.class)
                .hasMessage(noneExistingQuote.getMessage());
    }

    @Test
    public void should_add_generated_ids_when_saving_policy_for_first_time() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Policy policy = policyService.createPolicy(quote);
        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getId()).isNotNull();
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() throws Exception {
        Payment payment = paymentRepository.save(payment(100.0, "THB"));
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        Payment updatedPayment = policyService.updatePayment(payment.getPaymentId(), 100.0, "THB", null, ERROR, LINE, null, null, "Error msg");

        assertThat(updatedPayment.getEffectiveDate()).isNull();
        assertThat(updatedPayment.getPaymentInformations()).hasSize(1);
        assertThat(updatedPayment.getPaymentInformations()).extracting("rejectionErrorMessage").containsOnly("Error msg");
        assertThat(updatedPayment.getStatus()).isEqualTo(INCOMPLETE);
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
        assertThat(payment.getStatus()).isEqualTo(INCOMPLETE);
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

    @Test
    public void should_create_bytes_for_eReceipt() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        TestUtil.policy(policy);

        byte[] bytes = policyService.createEreceipt(policy);
        assertThat(bytes).isNotNull();
    }

    @Test
    public void should_create_bytes_for_eReceipt_and_can_create_pdf_file_to_file_system() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), getCommonData(), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        TestUtil.policy(policy);

        byte[] bytes = policyService.createEreceipt(policy);
        assertThat(bytes).isNotNull();

        StringBuilder im = new StringBuilder(storePath);
        im.append(File.separator + ERECEIPT_PDF_FILE_NAME);
        im.insert(im.toString().indexOf("."), "_" + policy.getPolicyId());
        String resultFileName = im.toString();

        ImageUtil.imageToPDF(bytes, resultFileName);
        File file = new File(resultFileName);
        assertThat(file.exists()).isTrue();
    }
}