package th.co.krungthaiaxa.api.elife.test.products.igen;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.service.ApplicationFormService;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyDocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.GreenMailUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author khoi.tran on 10/3/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class IGenApplicationFormTest extends ELifeTest {
    @Autowired
    private PolicyService policyService;
    @Autowired
    private PolicyFactory policyFactory;

    @Autowired
    private DocumentService documentService;
    @Autowired
    private PolicyDocumentService policyDocumentService;

    @Autowired
    private ApplicationFormService applicationFormService;

    @Autowired
    private QuoteFactory quoteFactory;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Before
    public void setup() {
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    @Test
    public void test_generate_applicationForm_for_not_validated_quote() throws IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIGen();
        Policy policy = policyService.createPolicy(quoteResult.getQuote());
        policyDocumentService.generateDocumentsForPendingValidationPolicy(policy);

        byte[] pdfContent = applicationFormService.generateNotValidatedApplicationForm(policy);
        File file = new File(TestUtil.PATH_TEST_RESULT + System.currentTimeMillis() + "_applicationform_" + policy.getPolicyId() + ".pdf");
        FileUtils.writeByteArrayToFile(file, pdfContent);
    }

    @Test
    public void test_generate_applicationForm_for_validated_quote_end_of_contract() throws IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIGen();
        Policy policy = policyFactory.createPolicyWithValidatedStatus(quoteResult.getQuote());
        testGenerateValidatedApplicationForm(policy);
    }

    @Test
    public void test_generate_applicationForm_for_validated_quote_annual_cash_back() throws IOException {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        Policy policy = policyFactory.createPolicyWithValidatedStatus(productQuotation, TestUtil.DUMMY_EMAIL);
        testGenerateValidatedApplicationForm(policy);
    }

    @Test
    public void test_generate_applicationForm_for_validated_quote_annual_next_premium() throws IOException {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.ANNUAL_PAY_BACK_NEXT_PREMIUM);
        Policy policy = policyFactory.createPolicyWithValidatedStatus(productQuotation, TestUtil.DUMMY_EMAIL);
        testGenerateValidatedApplicationForm(policy);
    }

    private void testGenerateValidatedApplicationForm(Policy policy) throws IOException {
        byte[] pdfContent = applicationFormService.generateValidatedApplicationForm(policy);
        File file = new File(TestUtil.PATH_TEST_RESULT + System.currentTimeMillis() + "_applicationform_" + policy.getPolicyId() + ".pdf");
        FileUtils.writeByteArrayToFile(file, pdfContent);
        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "emails");
    }
}
