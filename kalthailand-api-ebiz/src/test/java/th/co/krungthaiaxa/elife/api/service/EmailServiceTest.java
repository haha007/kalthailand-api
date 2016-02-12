package th.co.krungthaiaxa.elife.api.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EmailServiceTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${email.smtp.server}")
    private String smtp;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject}")
    private String subject;
    @Value("${lineid}")
    private String lineURL;
    @Inject
    private EmailService emailService;
    @Inject
    private QuoteService quoteService;

    private String base64Graph;

    @Before
    public void setup() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/graph.jpg");
        base64Graph = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    @Test
    public void should_send_email_containing_amounts_for_1_million_baht_with_insured_of_35_years_old() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_MONTH, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendEmail(quote, base64Graph, smtp, emailName, subject, lineURL);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(GreenMailUtil.getBody(email), "quoted-printable");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >2,968,718.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >3,043,171.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >3,062,746.00 บาท</td></tr>");
    }

    public static String decodeSimpleBody(String encodedBody, String encoding) throws MessagingException, IOException {
        if (encoding == null) {
            return null;
        }
        InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(encodedBody.getBytes("UTF-8")), encoding);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[encodedBody.length()];
        int last = bufferedInputStream.read(bytes);
        return new String(bytes, 0, last);
    }

}
