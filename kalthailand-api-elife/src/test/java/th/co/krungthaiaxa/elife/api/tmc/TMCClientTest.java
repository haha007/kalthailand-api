package th.co.krungthaiaxa.elife.api.tmc;


import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.service.DocumentService;
import th.co.krungthaiaxa.elife.api.service.PolicyService;
import th.co.krungthaiaxa.elife.api.service.QuoteService;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class TMCClientTest {
    @Inject
    private DocumentService documentService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private TMCClient tmcClient;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void should_send_ereceipt_to_tmc() {
        Policy policy = getPolicy();

        policyService.updatePolicyAfterFirstPaymentValidated(policy);
        policyService.updatePolicyAfterPolicyHasBeenValidated(policy, "agentCode");
        Document document = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst().get();
        String documentContent = documentService.downloadDocument(document.getId()).getContent();

        // should not throw any exception
        tmcClient.sendPDFToTMC(policy, documentContent, ERECEIPT_PDF);
    }

    private Policy getPolicy() {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
