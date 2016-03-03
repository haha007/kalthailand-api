package th.co.krungthaiaxa.elife.api.service;


import org.apache.commons.lang3.tuple.Pair;
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
import th.co.krungthaiaxa.elife.api.model.PaymentInformation;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.products.Product10EC;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_HALF_YEAR;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExportServiceTest {
    private final static String ERECEIPT_PDF_FILE_NAME = "ereceipt.pdf";
    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;
    @Inject
    private ExportService exportService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PaymentRepository paymentRepository;
    private Product10EC product10EC = new Product10EC();

    @Test
    public void should_create_excel_file_with_one_sheet_only_with_2_lines() throws Exception {
        Policy policy = getPolicy();
        PaymentInformation paymentInformation = new PaymentInformation();
//        paymentInformation.setAmount();
//        paymentInformation.setDate();
//        paymentInformation.setRejectionErrorMessage();
//        paymentInformation.setStatus();

        List<Pair<Policy, PaymentInformation>> payments = new ArrayList<>();
        payments.add(Pair.of(policy, paymentInformation));

        byte[] content = exportService.exportPayments(payments);
        assertThat(content).isNotNull();
    }

    private Policy getPolicy() throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_HALF_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }

}
