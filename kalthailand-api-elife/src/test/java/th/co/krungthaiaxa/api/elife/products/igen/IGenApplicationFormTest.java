package th.co.krungthaiaxa.api.elife.products.igen;

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
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.line.LinePayCaptureMode;
import th.co.krungthaiaxa.api.elife.service.ApplicationFormService;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.PolicyValidatedProcessingService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

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
    private IGenService productService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private PolicyService policyService;
    @Autowired
    private PolicyFactory policyFactory;
    @Autowired
    private PolicyValidatedProcessingService policyValidatedProcessingService;

    @Autowired
    private DocumentService documentService;

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
        documentService.generateNotValidatedPolicyDocuments(policy);

        byte[] pdfContent = applicationFormService.generateNotValidatedApplicationForm(policy);
        File file = new File("testresult/" + System.currentTimeMillis() + "_applicationform_" + policy.getPolicyId() + ".pdf");
        FileUtils.writeByteArrayToFile(file, pdfContent);
    }

    @Test
    public void test_generate_applicationForm_for_validated_quote() throws IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIGen();
        Policy policy = policyService.createPolicy(quoteResult.getQuote());
        String agentCode = "999999-99-999999";
        String agentName = "Mock Agent";
        policy = policyValidatedProcessingService.processValidatedPolicy(new PolicyValidatedProcessingService.PolicyValidationRequest(policy.getPolicyId(), agentCode, agentName, LinePayCaptureMode.FAKE_WITH_SUCCESS, RequestFactory.generateAccessToken()));
        byte[] pdfContent = applicationFormService.generateValidatedApplicationForm(policy);
        File file = new File("testresult/" + System.currentTimeMillis() + "_applicationform_" + policy.getPolicyId() + ".pdf");
        FileUtils.writeByteArrayToFile(file, pdfContent);
    }
}
