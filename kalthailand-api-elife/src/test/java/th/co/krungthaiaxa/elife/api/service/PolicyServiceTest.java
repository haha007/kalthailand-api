package th.co.krungthaiaxa.elife.api.service;


import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
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
import th.co.krungthaiaxa.elife.api.products.Product10EC;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.resource.TestUtil;
import th.co.krungthaiaxa.elife.api.utils.ImageUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.emptyQuote;
import static th.co.krungthaiaxa.elife.api.exception.PolicyValidationException.noneExistingQuote;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.SuccessErrorStatus.SUCCESS;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PolicyServiceTest {
    private final static String ERECEIPT_PDF_FILE_NAME = "ereceipt.pdf";
    @Value("${path.store.watermarked.image}")
    private String storePath;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PaymentRepository paymentRepository;
    private Product10EC product10EC = new Product10EC();

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

        Quote quote1 = quoteService.createQuote(sessionId, product10EC.getCommonData(), LINE);
        quote(quote1, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote1 = quoteService.updateQuote(quote1);

        Quote quote2 = quoteService.createQuote(sessionId, product10EC.getCommonData(), LINE);
        quote(quote2, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
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
    public void should_add_only_one_document_of_type_ereceipt_when_updating_payment() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);

        assertThat(policy.getDocuments()).extracting("typeName").containsExactly("ERECEIPT");
    }

    @Test
    public void should_mark_payment_as_incomplete_when_with_error_status() throws Exception {
        Policy policy = getPolicy();

        Payment payment = policy.getPayments().get(0);
        payment.getAmount().setValue(100.0);
        assertThat(payment.getStatus()).isEqualTo(FUTURE);

        policyService.updatePayment(policy, payment, 100.0, "THB", null, ERROR, LINE, null, null, "Error msg");
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

        policyService.updatePayment(policy, payment, 100.0, "THB", null, SUCCESS, LINE, null, null, null);

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

        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);

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

        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(policy, payment, 75.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(policy, payment, -25.0, "THB", null, SUCCESS, LINE, null, null, null);

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

        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);
        policyService.updatePayment(policy, payment, 50.0, "THB", null, ERROR, LINE, null, null, null);
        policyService.updatePayment(policy, payment, 50.0, "THB", null, SUCCESS, LINE, null, null, null);

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

        policyService.updatePayment(policy, payment, 100.0, "EUR", null, SUCCESS, LINE, null, null, null);

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

        policyService.updatePayment(policy, payment, 150.0, "THB", null, SUCCESS, LINE, null, null, null);

        assertThat(payment.getEffectiveDate()).isEqualTo(payment.getDueDate());
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations()).extracting("rejectionErrorMessage").containsNull();
        assertThat(payment.getStatus()).isEqualTo(OVERPAID);
    }

    @Test
    public void should_create_bytes_for_eReceipt() throws QuoteCalculationException, PolicyValidationException, IOException {
        Policy policy = getPolicy();
        TestUtil.policy(policy);

        byte[] bytes = policyService.createEreceipt(policy);
        assertThat(bytes).isNotNull();
    }

    @Test
    public void should_create_bytes_for_eReceipt_and_can_create_pdf_file_to_file_system() throws
            QuoteCalculationException, PolicyValidationException, IOException, DocumentException {
        Policy policy = getPolicy();
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

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), product10EC.getCommonData(), LINE);
        quote(quote, EVERY_HALF_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }

}
