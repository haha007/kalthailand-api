package th.co.krungthaiaxa.api.elife.test.thirdParty.mocab;

import org.junit.Assert;
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
import th.co.krungthaiaxa.api.elife.model.MocabStatus;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.thirdParty.mocab.MocabClient;
import th.co.krungthaiaxa.api.elife.thirdParty.mocab.MocabResponse;

import javax.inject.Inject;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Created by tuong.le on 3/6/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class MocabClientTest extends ELifeTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyFactory policyFactory;
    @Inject
    private MocabClient mocabClient;

    @Test
    public void should_send_ereceipt_to_mocab() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Policy policy = policyFactory
                .createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefault());
        Document document = policy.getDocuments()
                .stream()
                .filter(tmp -> tmp.getTypeName().equals(DocumentType.ERECEIPT_PDF))
                .findFirst()
                .get();
        String documentContent = documentService.findDocumentDownload(document.getId()).getContent();

        // should not throw any exception
        Optional<MocabResponse> responseOptional =
                mocabClient.sendPdfToMocab(policy, documentContent, DocumentType.ERECEIPT_PDF);
        Assert.assertTrue(responseOptional.isPresent()
                && responseOptional.get().getMessageCode().equals(MocabStatus.SUCCESS));
    }


}
