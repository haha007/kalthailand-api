package th.co.krungthaiaxa.elife.api.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.TestUtil;
import th.co.krungthaiaxa.elife.api.utils.ImageUtil;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.Base64;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EmailServiceTest {
    private final static String ERECEIPT_MERGED_FILE_NAME = "ereceipts.pdf";
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
    @Value("${tmp.path.deleted.after.tests}")
    private String tmpPathDeletedAfterTests;
    @Value("${button.url.ereceipt.mail}")
    private String buttonUrlEreceiptMail;
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
    public void should_send_quote_email_with_proper_from_address() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    @Test
    public void should_send_quote_email_to_insured_email_address() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getRecipients(Message.RecipientType.TO)).containsOnly(new InternetAddress(quote.getInsureds().get(0).getPerson().getEmail()));
    }

    @Test
    public void should_send_quote_email_containing_amounts_for_1_million_baht_with_insured_of_35_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >2,018,995.00 บาท");
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >2,253,818.00 บาท");
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >2,302,771.00 บาท");
    }

    @Test
    public void should_send_quote_email_containing_amounts_for_500_thousand_baht_with_insured_of_55_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 500000.0, insured(55), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >1,009,498.00 บาท");
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,126,910.00 บาท");
        assertThat(bodyAsString).contains("รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,151,386.00 บาท");
    }

    @Test
    public void should_send_quote_email_with_product_information() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 500000.0, insured(55), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาคุ้มครอง</td><td width=\"120px\" class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfCoverage().toString() + " ปี</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาชำระเบี้ย</td><td class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfPremium().toString() + " ปี</td></tr>");

        Multipart multipart = (Multipart) email.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            } else {
                assertThat(null != bodyPart.getFileName() && !bodyPart.getFileName().equals(""));
            }
        }
    }

    @Test
    public void generate_sale_illustration_pdf_file() throws Exception {

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 1000000.0, insured(30), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuoteEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subject);
        String bodyAsString = decodeSimpleBody(getBody(email));
        //System.out.println(quote.getQuoteId());
        //System.out.println(bodyAsString);
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาคุ้มครอง</td><td width=\"120px\" class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfCoverage().toString() + " ปี</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาชำระเบี้ย</td><td class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfPremium().toString() + " ปี</td></tr>");

        Multipart multipart = (Multipart) email.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            } else {
                assertThat(null != bodyPart.getFileName() && !bodyPart.getFileName().equals(""));
            }
        }
    }

    @Test
    public void should_send_ereceipt_pdf_file_attachment_in_email() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE, productQuotation());
        quote(quote, EVERY_YEAR, 1000000.0, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        TestUtil.policy(policy);

        byte[] bytes = policyService.createEreceipt(policy);
        assertThat(bytes).isNotNull();

        FileUtils.forceMkdir(new File(tmpPathDeletedAfterTests));
        tmpPathDeletedAfterTests = tmpPathDeletedAfterTests + getProperty("file.separator") + ERECEIPT_MERGED_FILE_NAME;
        StringBuilder im = new StringBuilder(tmpPathDeletedAfterTests);
        im.insert(tmpPathDeletedAfterTests.indexOf("."), "_" + policy.getPolicyId());
        tmpPathDeletedAfterTests = im.toString();

        ImageUtil.imageToPDF(bytes, tmpPathDeletedAfterTests);
        File file = new File(tmpPathDeletedAfterTests);
        assertThat(file.exists()).isTrue();

        emailService.sendEreceiptEmail(policy, Pair.of(readFileToByteArray(file), file.getName()));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + "<br/>");
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต");
        assertThat(bodyAsString).contains("<a class=\"btn\" href=\"" + buttonUrlEreceiptMail + "\">UPLOAD YOUR ID NOW</a>");

        Multipart multipart = (Multipart) email.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            } else {
                assertThat(null != bodyPart.getFileName() && !bodyPart.getFileName().equals(""));
            }
        }
    }

    private static String decodeSimpleBody(String encodedBody) throws MessagingException, IOException {
        InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(encodedBody.getBytes("UTF-8")), "quoted-printable");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[encodedBody.length()];
        int last = bufferedInputStream.read(bytes);
        return new String(bytes, 0, last);
    }


}
