package th.co.krungthaiaxa.api.elife.products.igen;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.productQuotation;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
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

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.itextpdf.text.pdf.PdfReader;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.AbstractProductEmailService;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class IGenEmailServiceTest {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(IGenEmailServiceTest.class);	
	@Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);	
	@Inject
    private QuoteService quoteService;	
	@Inject
	private IGenEmailService iGenEmailService;
	
	@Before
    public void setup() {
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }
	
	@Test
	public void should_send_quote_email() throws MessagingException, IOException{
		ProductQuotation productQuotation = productQuotation(ProductType.PRODUCT_IGEN, 30, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 5, GenderCode.MALE);
		Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation);
		quote.getInsureds().get(0).getPerson().setEmail("dummy@krungthai-axa.co.th");		
		iGenEmailService.sendQuoteEmail(quote);		
		MimeMessage email = greenMail.getReceivedMessages()[0];
		assertThat(decodeSimpleBody(getBody(email))).isNotNull();
	}

}
