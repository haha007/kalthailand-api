package th.co.krungthaiaxa.elife.api.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.itextpdf.text.pdf.PdfReader;
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
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EmailServiceTest {
    private final static String ERECEIPT_MERGED_FILE_NAME = "e-receipts.pdf";
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${email.smtp.server}")
    private String smtp;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject.quote}")
    private String subjectQuote;
    @Value("${line.app.id}")
    private String lineId;
    @Value("${button.url.ereceipt.mail}")
    private String buttonUrlEreceiptMail;
    @Inject
    private DocumentService documentService;
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
    public void should_send_quote_ifine_email_with_proper_from_address() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 55, EVERY_YEAR, 100000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        quote.getInsureds().get(0).getPerson().setEmail("santi.lik@krungthai-axa.co.th");
        emailService.sendQuoteiFineEmail(quote);
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    @Test
    public void should_send_quote_10ec_email_with_proper_from_address() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    @Test
    public void should_send_email_all_scenario() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);

        policy.getInsureds().get(0).getPerson().setEmail("tossaphol.chi@krungthai-axa.co.th");

        emailService.sendPolicyBookedEmail(policy);
        emailService.sendUserNotRespondingEmail(policy);
        emailService.sendPhoneNumberIsWrongEmail(policy);

        documentService.generateValidatedPolicyDocuments(policy);
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();
        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();
        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-10ec.pdf"));
    }

    @Test
    public void should_send_quote_10ec_email_to_insured_email_address() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getRecipients(Message.RecipientType.TO)).containsOnly(new InternetAddress(quote.getInsureds().get(0).getPerson().getEmail()));
    }

    @Test
    public void should_send_quote_10ec_email_containing_amounts_for_1_million_baht_with_insured_of_35_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >706,649.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >788,837.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >805,971.00 บาท</td></tr>");
    }

    @Test
    public void should_send_quote_10ec_email_containing_amounts_for_500_thousand_baht_with_insured_of_55_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(55, EVERY_YEAR, 500000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >1,009,498.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,126,910.00 บาท</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,151,386.00 บาท</td></tr>");
    }

    @Test
    public void should_send_quote_email_containing_links_to_line_app() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(55, EVERY_YEAR, 500000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("href='https://line.me/R/ch/" + lineId + "/elife/th/'");
        assertThat(bodyAsString).contains("href='https://line.me/R/ch/" + lineId + "/elife/th/fatca-questions/" + quote.getQuoteId() + "'");
        assertThat(bodyAsString).contains("https://line.me/R/ch/" + lineId + "/elife/th/quote-product/line-10-ec'");
    }

    @Test
    public void should_send_quote_10ec_email_with_product_information() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(55, EVERY_YEAR, 500000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาคุ้มครอง</td><td width=\"120px\" class=\"value\" align=\"right\" >10 ปี</td></tr>");
        assertThat(bodyAsString).contains("<tr><td>ระยะเวลาชำระเบี้ย</td><td class=\"value\" align=\"right\" >6 ปี</td></tr>");

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
    public void generate_sale_illustration_10ec_pdf_file() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
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
    public void should_send_policy_booked_email_with_proper_from_address() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);

        emailService.sendPolicyBookedEmail(policy);
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    @Test
    public void should_send_10ec_ereceipt_pdf_file_attachment_in_email() throws Exception {
        Quote quote = quoteService.createQuote(RandomStringUtils.randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        policy(policy);
        policy.getPayments().get(0).setEffectiveDate(LocalDate.now());

        documentService.generateValidatedPolicyDocuments(policy);
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();

        FileUtils.writeByteArrayToFile(new File("target/e-receipt-10ec.pdf"), bytes);

        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-10ec.pdf"));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject-10ec.txt"), Charset.forName("UTF-8")));
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + "<br/>");
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต");

        Multipart multipart = (Multipart) email.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            } else {
                assertThat(bodyPart.getFileName()).isEqualTo("emailServiceTest-e-receipt-10ec.pdf");
            }
        }
    }

    @Test
    public void should_send_ifine_ereceipt_pdf_file_attachment_in_email() throws Exception {
        Quote quote = quoteService.createQuote("xxx", LINE, productQuotation(PRODUCT_IFINE, 50, EVERY_MONTH, 10000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);
        Policy policy = policyService.createPolicy(quote);
        policy(policy);
        policy.getPayments().get(0).setEffectiveDate(LocalDate.now());

        documentService.generateValidatedPolicyDocuments(policy);
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();

        FileUtils.writeByteArrayToFile(new File("target/e-receipt-ifine.pdf"), bytes);

        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-ifine.pdf"));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject-ifine.txt"), Charset.forName("UTF-8")));
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + "<br/>");
        assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต");

        Multipart multipart = (Multipart) email.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    !StringUtils.isNotBlank(bodyPart.getFileName())) {
                //null file value
            } else {
                assertThat(bodyPart.getFileName()).isEqualTo("emailServiceTest-e-receipt-ifine.pdf");
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
