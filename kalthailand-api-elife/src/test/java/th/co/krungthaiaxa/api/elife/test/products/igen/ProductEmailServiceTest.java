package th.co.krungthaiaxa.api.elife.test.products.igen;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductEmailService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.utils.GreenMailUtil;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductEmailServiceTest extends ELifeTest {

    protected final static Logger LOGGER = LoggerFactory.getLogger(ProductEmailServiceTest.class);
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Inject
    private QuoteService quoteService;
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private ProductEmailService productEmailService;

    @Before
    public void setup() {
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    @Test
    public void igen_send_quote_email() throws MessagingException, IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIGen("dummy@krungthai-axa.co.th");
        test_send_quote_email(quoteResult);
    }

    @Test
    public void iprotect_send_quote_email() throws MessagingException, IOException {
        QuoteFactory.QuoteResult quoteResult = quoteFactory.createDefaultIProtect("dummy@krungthai-axa.co.th");
        test_send_quote_email(quoteResult);
    }

    private void test_send_quote_email(QuoteFactory.QuoteResult quoteResult) throws IOException, MessagingException {
        Quote quote = quoteResult.getQuote();
        Date date = new Date();
        productEmailService.sendQuoteEmail(quote.getQuoteId(), quoteResult.getSessionId(), quoteResult.getChannelType(), null);
        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "emails");
        assertSentEmail(greenMail);
    }

    private void assertSentEmail(GreenMailRule greenMail) {
        List<MimeMessage> emails = Arrays.asList(greenMail.getReceivedMessages());
        Assert.assertTrue(emails.size() > 0);
//        List<MimeMessage> sentEmailsAfterDate = emails.stream().filter(email -> {
//            try {
//                return email.getSentDate().after(date) || email.getSentDate().equals(date);
//            } catch (MessagingException e) {
//                return false;
//            }
//        }).collect(Collectors.toList());
//        Assert.assertTrue(sentEmailsAfterDate.size() > 0);
    }
}
