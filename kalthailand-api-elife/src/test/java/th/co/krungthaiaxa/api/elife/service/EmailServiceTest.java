package th.co.krungthaiaxa.api.elife.service;

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
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static th.co.krungthaiaxa.api.elife.TestUtil.*;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.products.ProductType.PRODUCT_IFINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EmailServiceTest extends ELifeTest {
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
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;

    private String base64Graph;

    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    @Before
    public void setup() {
        InputStream inputStream = this.getClass().getResourceAsStream("/graph.jpg");
        try {
            base64Graph = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            // Ignore
        }
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
        quote = quoteService.updateQuote(quote, "token");
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
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));
    }

    //===========================================================================================

    @Test
    public void should_send_booked_email() throws Exception {

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy.getInsureds().get(0).getPerson().setEmail("tanawat_hemchua@hotmail.com");
        emailService.sendPolicyBookedEmail(policy);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email1 = greenMail.getReceivedMessages()[0];
        String bodyAsString1 = decodeSimpleBody(getBody(email1));
        //assertThat(bodyAsString1).contains("<td>ระบบได้ทำการจองวงเงินผ่าน LINE Pay สำเร็จแล้ว เจ้าหน้าที่ของเราจะติดต่อกลับภายใน 1 ชั่วโมง* ด้วยหมายเลข <span class=\"under-line-text\" >02-770-3599</span></td>");
        //assertThat(bodyAsString1).contains("<tr><td align=\"center\" class=\"header-text\">ขอบคุณ คุณ" + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + " </td></tr>");
        //assertThat("<div style=\"text-align:center;\" class=\"header-text\">" + policy.getPolicyId() + "</div>");
        //assertThat("<td align=\"right\" class=\"header-normal\" >" + messageSource.getMessage("product.id." + policy.getCommonData().getProductId(), null, thLocale) + " (" + policy.getCommonData().getProductId() + ")" + "</td>");
        //assertThat("<td align=\"right\" class=\"header-normal\" >" + String.valueOf(policy.getCommonData().getNbOfYearsOfPremium()) + " ปี</td>");
        //assertThat("<td align=\"right\" class=\"header-normal\" >" + messageSource.getMessage("payment.mode." + policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale) + "</td>");
        DecimalFormat money = new DecimalFormat("#,##0");
        String sumInsure = "";
        if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            sumInsure = money.format(policy.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue());
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            sumInsure = money.format(policy.getPremiumsData().getProductIFinePremium().getSumInsured().getValue());
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IGEN.getName())) {
            sumInsure = money.format(policy.getPremiumsData().getProductIGenPremium().getSumInsured().getValue());
        }
        assertThat("<td align=\"right\" class=\"header-normal\" >" + sumInsure + " บาท</td>");
        assertThat("<td align=\"right\" class=\"header-normal\" >" + money.format(policy.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท</td>");

    }

    @Test
    public void should_send_wrong_number_email() throws Exception {

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy.getInsureds().get(0).getPerson().setEmail("tanawat_hemchua@hotmail.com");
        emailService.sendPhoneNumberIsWrongEmail(policy);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email3 = greenMail.getReceivedMessages()[0];
        String bodyAsString3 = decodeSimpleBody(getBody(email3));
        //assertThat(bodyAsString3).contains("เนื่องจากเจ้าหน้าที่ไม่สามารถทำการติดต่อท่าน ผ่านหมายเลขโทรศัพท์ที่ท่านระบุไว้ กรุณายืนยันหมายเลขโทรศัพท์ของท่านอีกครั้ง โทร <span class=\"under-line-text\" >02-770-3599</span> ระหว่างเวลา 8.30-19.00 น.");
        assertThat("<tr><td align=\"center\" class=\"header-text\">ขอบคุณ คุณ" + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + " </td></tr>");
        assertThat("<div style=\"text-align:center;\" class=\"header-text\">" + policy.getPolicyId() + "</div>");

    }

    @Test
    public void should_send_not_response_email() throws Exception {

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy.getInsureds().get(0).getPerson().setEmail("tanawat_hemchua@hotmail.com");
        emailService.sendUserNotRespondingEmail(policy);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email2 = greenMail.getReceivedMessages()[0];
        String bodyAsString2 = decodeSimpleBody(getBody(email2));
        //assertThat(bodyAsString2).contains("เนื่องจากเจ้าหน้าที่ไม่สามารถติดต่อท่านได้ เจ้าหน้าที่จะทำการติดต่อท่านอีกครั้งด้วยหมายเลข <span class=\"under-line-text\" >02-770-3599</span> ภายในเวลาทำการ 8.30-19.00 น.");
        assertThat("<tr><td align=\"center\" class=\"header-text\">ขอบคุณ คุณ\" + policy.getInsureds().get(0).getPerson().getGivenName() + \" \" + policy.getInsureds().get(0).getPerson().getSurName() + \" </td></tr>");
        assertThat("<div style=\"text-align:center;\" class=\"header-text\">" + policy.getPolicyId() + "</div>");

    }

    @Test
    public void should_send_ereceipt_email() throws Exception {

        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy.getInsureds().get(0).getPerson().setEmail("tanawat_hemchua@hotmail.com");

        documentService.generateValidatedPolicyDocuments(policy, "token");
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();
        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();
        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-10ec.pdf"));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email4 = greenMail.getReceivedMessages()[0];
        String bodyAsString4 = decodeSimpleBody(getBody(email4));
        //assertThat(bodyAsString4).contains("บริษัท กรุงไทย-แอกซ่า ประกันชีวิต จำกัด (มหาชน) ขอขอบคุณท่านที่วางใจโดยทำการสมัครแบบประกันชีวิต " + messageSource.getMessage("product.id." + policy.getCommonData().getProductId(), null, thLocale) + " (" + policy.getCommonData().getProductId() + ")" + " ผ่าน LINE Pay");
        assertThat("<tr><td align=\"center\" class=\"header-text\">ที่วางใจ และทำการสมัครแบบประกัน messageSource.getMessage(\"product.id.\" + policy.getCommonData().getProductId(), null, thLocale) + \" (\" + policy.getCommonData().getProductId() + \")\"</td></tr>");
        assertThat("<tr><td>เรียนคุณ " + policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName() + " </td></tr>");

        Multipart multipart = (Multipart) email4.getContent();
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
    public void should_send_quote_10ec_email_to_insured_email_address() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getRecipients(Message.RecipientType.TO)).containsOnly(new InternetAddress(quote.getInsureds().get(0).getPerson().getEmail()));
    }

    @Test
    public void should_send_quote_10ec_email_containing_amounts_for_1_million_baht_with_insured_of_35_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >706,649.00 บาท</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >788,837.00 บาท</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >805,971.00 บาท</td></tr>");
    }

    @Test
    public void should_send_quote_10ec_email_containing_amounts_for_500_thousand_baht_with_insured_of_55_years_old() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(55, EVERY_YEAR, 500000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ขั้นต่ำ</td><td class=\"value\" align=\"right\" valign=\"top\" >1,009,498.00 บาท</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับกลาง (รวมเงินปันผล**ระดับกลาง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,126,910.00 บาท</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>รวมรับผลประโยชน์ระดับสูง (รวมเงินปันผล**ระดับสูง)</td><td class=\"value\" valign=\"top\" align=\"right\" >1,151,386.00 บาท</td></tr>");
    }

    @Test
    public void should_send_quote_email_containing_links_to_line_app() throws Exception {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(55, EVERY_YEAR, 500000.0));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

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
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td>ระยะเวลาคุ้มครอง</td><td width=\"120px\" class=\"value\" align=\"right\" >10 ปี</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>ระยะเวลาชำระเบี้ย</td><td class=\"value\" align=\"right\" >6 ปี</td></tr>");

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
        quote = quoteService.updateQuote(quote, "token");

        emailService.sendQuote10ECEmail(quote, base64Graph);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        assertThat(email.getSubject()).isEqualTo(subjectQuote);
        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td>ระยะเวลาคุ้มครอง</td><td width=\"120px\" class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfCoverage().toString() + " ปี</td></tr>");
        //assertThat(bodyAsString).contains("<tr><td>ระยะเวลาชำระเบี้ย</td><td class=\"value\" align=\"right\" >" + quote.getCommonData().getNbOfYearsOfPremium().toString() + " ปี</td></tr>");

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
        quote = quoteService.updateQuote(quote, "token");
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
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy(policy);
        policy.getPayments().get(0).setEffectiveDate(LocalDate.now());

        documentService.generateValidatedPolicyDocuments(policy, "token");
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();

        FileUtils.writeByteArrayToFile(new File("target/e-receipt-10ec.pdf"), bytes);

        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-10ec.pdf"));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        String testSubject = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject.txt"), Charset.forName("UTF-8"));
        testSubject = testSubject.replace("%PRODUCT%", messageSource.getMessage("product.id." + policy.getCommonData().getProductId(), null, thLocale));
        assertThat(email.getSubject()).isEqualTo(testSubject);
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td align=\"center\" class=\"header-text\">กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ</td></tr>");
        //assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต");

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
        quote = quoteService.updateQuote(quote, "token");
        Policy policy = policyService.createPolicy(quote);
        policy(policy);
        policy.getPayments().get(0).setEffectiveDate(LocalDate.now());

        documentService.generateValidatedPolicyDocuments(policy, "token");
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        assertThat(documentPdf.isPresent()).isTrue();

        DocumentDownload documentDownload = documentService.downloadDocument(documentPdf.get().getId());
        byte[] bytes = Base64.getDecoder().decode(documentDownload.getContent());
        assertThat(new PdfReader(bytes)).isNotNull();

        FileUtils.writeByteArrayToFile(new File("target/e-receipt-ifine.pdf"), bytes);

        emailService.sendEreceiptEmail(policy, Pair.of(bytes, "emailServiceTest-e-receipt-ifine.pdf"));

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage email = greenMail.getReceivedMessages()[0];
        String testSubject = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject.txt"), Charset.forName("UTF-8"));
        testSubject = testSubject.replace("%PRODUCT%", messageSource.getMessage("product.id." + policy.getCommonData().getProductId(), null, thLocale));
        assertThat(email.getSubject()).isEqualTo(testSubject);
        assertThat(email.getFrom()).containsOnly(new InternetAddress(emailName));

        String bodyAsString = decodeSimpleBody(getBody(email));
        //assertThat(bodyAsString).contains("<tr><td align=\"center\" class=\"header-text\">กรุงไทย-แอกซ่า ประกันชีวิต ขอขอบคุณ</td></tr>");
        //assertThat(bodyAsString).contains("กรุงไทย-แอกซ่า ประกันชีวิต");

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
