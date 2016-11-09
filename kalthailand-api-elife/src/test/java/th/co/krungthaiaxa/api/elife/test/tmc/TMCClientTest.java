package th.co.krungthaiaxa.api.elife.test.tmc;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class TMCClientTest extends ELifeTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyFactory policyFactory;
    @Inject
    private TMCClient tmcClient;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void should_send_ereceipt_to_tmc() {
        Policy policy = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefault());
        Document document = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(DocumentType.ERECEIPT_PDF)).findFirst().get();
        String documentContent = documentService.findDocumentDownload(document.getId()).getContent();

        // should not throw any exception
        tmcClient.sendPDFToTMC(policy, documentContent, DocumentType.ERECEIPT_PDF);
    }

}
