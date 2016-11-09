package th.co.krungthaiaxa.api.elife.test.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.service.PaymentService;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptPdfService;

/**
 * @author khoi.tran on 10/25/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EreceiptPdfServiceTest extends ELifeTest {
    @Autowired
    private EreceiptPdfService ereceiptPdfService;
    @Autowired
    private PolicyFactory policyFactory;
    @Autowired
    private QuoteFactory quoteFactory;
    @Autowired
    private PaymentService paymentService;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void testGeneratePdf_by_monthly() {
        Policy policy = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment(), "dummy@gmail.com");
        Payment payment = paymentService.findFirstPaymentHasTransactionId(policy.getPolicyId());
        byte[] pdfBytes = ereceiptPdfService.createEreceiptPdf(policy, payment, true);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/pdf/ereceipt_" + DateTimeUtil.formatNowForFilePath() + "_" + policy.getPolicyId() + ".pdf", pdfBytes);
    }

    @Test
    public void testGeneratePdf_by_quarter() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(30, PeriodicityCode.EVERY_QUARTER, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createQuote(productQuotation, "dummy@gmail.com");
        Policy policy = policyFactory.createPolicyWithValidatedStatus(quoteResult.getQuote());

        Payment payment = paymentService.findFirstPaymentHasTransactionId(policy.getPolicyId());
        byte[] pdfBytes = ereceiptPdfService.createEreceiptPdf(policy, payment, true);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/pdf/ereceipt_" + DateTimeUtil.formatNowForFilePath() + "_" + policy.getPolicyId() + "_quarter.pdf", pdfBytes);
    }

    @Test
    public void testGeneratePdf_by_halfyear() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(30, PeriodicityCode.EVERY_HALF_YEAR, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createQuote(productQuotation, "dummy@gmail.com");
        Policy policy = policyFactory.createPolicyWithValidatedStatus(quoteResult.getQuote());
        Payment payment = paymentService.findFirstPaymentHasTransactionId(policy.getPolicyId());
        byte[] pdfBytes = ereceiptPdfService.createEreceiptPdf(policy, payment, true);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/pdf/ereceipt_" + DateTimeUtil.formatNowForFilePath() + "_" + policy.getPolicyId() + "_halfyear.pdf", pdfBytes);

    }

}
