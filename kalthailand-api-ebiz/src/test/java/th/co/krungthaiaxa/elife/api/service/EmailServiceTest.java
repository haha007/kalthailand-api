package th.co.krungthaiaxa.elife.api.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.resource.TestUtil;
import th.co.krungthaiaxa.elife.api.utils.ImageUtil;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.Base64;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.UNABLE_TO_CREATE_ERECEIPT;
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
    @Value("${path.store.elife.ereceipt.pdf}")
    private String eReceiptPdfStorePath;
    @Inject
    private EmailService emailService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyService policyService;

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
    public void should_send_email_with_proper_from_address() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    @Test
    public void should_send_email_to_insured_email_address() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getRecipients(Message.RecipientType.TO)).containsOnly(new InternetAddress(quote.getInsureds().get(0).getPerson().getEmail()));
    }

    @Test
    public void should_send_email_containing_amounts_for_1_million_baht_with_insured_of_35_years_old() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >2,968,718.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >3,043,171.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >3,062,746.00 บาท</td></tr>");
    }

    @Test
    public void should_send_email_containing_amounts_for_500_thousand_baht_with_insured_of_55_years_old() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 500000.0, insured(55, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >1,484,359.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,521,594.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,531,376.00 บาท</td></tr>");
    }

    public static String decodeSimpleBody(String encodedBody) throws MessagingException, IOException {
        InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(encodedBody.getBytes("UTF-8")), "quoted-printable");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[encodedBody.length()];
        int last = bufferedInputStream.read(bytes);
        return new String(bytes, 0, last);
    }

    @Test
    public void should_send_pdf_file_attachment_in_email() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE);
        quote(quote, EVERY_YEAR, 1000000.0, insured(35, Boolean.TRUE), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        TestUtil.policy(policy);

        byte[] bytes = policyService.createEreceipt(policy);
        assertThat(bytes).isNotNull();

        StringBuilder im = new StringBuilder(eReceiptPdfStorePath);
        im.insert(eReceiptPdfStorePath.indexOf("."), "_" + policy.getPolicyId());
        eReceiptPdfStorePath = im.toString();

        ImageUtil.imageToPDF(bytes, eReceiptPdfStorePath);
        File file = new File(eReceiptPdfStorePath);
        assertThat(file.exists()).isTrue();

        emailService.sendEreceiptEmail(policy,eReceiptPdfStorePath);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ "+policy.getInsureds().get(0).getPerson().getGivenName()+" "+policy.getInsureds().get(0).getPerson().getSurName()+"<br/>");
        assertThat(bodyAsString).contains("<tr><td>กรุงไทย-แอกซ่า ประกันชีวิต</td></tr>");

        Multipart multipart = (Multipart) email.getContent();
        for(int i=0; i < multipart.getCount(); i++){
            BodyPart bodyPart = multipart.getBodyPart(i);
            if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            }else{
                assertThat(null!=bodyPart.getFileName()&&!bodyPart.getFileName().equals(""));
            }
        }

    }



}
