package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.ProductIFinePremium;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.products.ProductType;
import th.co.krungthaiaxa.elife.api.utils.EmailSender;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.io.IOUtils.toByteArray;

@Service
public class EmailService {
    private final static Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final EmailSender emailSender;
    private final SaleIllustration10ECService saleIllustration10ECService;
    private final SaleIllustrationiFineService saleIllustrationiFineService;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject.quote}")
    private String subject;
    @Value("${line.app.id}")
    private String lineId;
    @Value("${button.url.ereceipt.mail}")
    private String uploadDocURL;

    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    @Inject
    public EmailService(EmailSender emailSender, SaleIllustration10ECService saleIllustration10ECService, SaleIllustrationiFineService saleIllustrationiFineService) {
        this.emailSender = emailSender;
        this.saleIllustration10ECService = saleIllustration10ECService;
        this.saleIllustrationiFineService = saleIllustrationiFineService;
    }

    public void sendQuote10ECEmail(Quote quote, String base64Image) throws IOException, MessagingException, DocumentException {
        logger.info("Sending quote 10EC email");
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(Base64.getDecoder().decode(base64Image), "<imageElife2>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitBlue.jpg")), "<benefitRed>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitGreen.jpg")), "<benefitGreen>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitPoint.jpg")), "<benefitPoint>"));
        //generate sale illustration 10EC pdf file
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(saleIllustration10ECService.generatePDF(quote, base64Image));
        emailSender.sendEmail(emailName, quote.getInsureds().get(0).getPerson().getEmail(), subject, getQuote10ECEmailContent(quote), base64ImgFileNames, attachments);
        logger.info("Quote email sent");
    }

    public void sendQuoteiFineEmail(Quote quote) throws IOException, MessagingException, DocumentException {
        logger.info("Sending quote iFine email");
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/logo.png")), "<imageLogo>"));
        //generate sale illustration iFine pdf file
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(saleIllustrationiFineService.generatePDF(quote));
        emailSender.sendEmail(emailName, quote.getInsureds().get(0).getPerson().getEmail(), subject, getQuoteiFineEmailContent(quote), base64ImgFileNames, attachments);
    }

    public void sendPolicyBookedEmail(Policy policy) throws IOException, MessagingException {
        logger.info("Sending policy booked email");
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), subject, "The content of this email has not been provided yet", new ArrayList<>(), new ArrayList<>());
    }

    public void sendUserNotRespondingEmail(Policy policy) throws IOException, MessagingException {
        logger.info("Sending user is not responding email");
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), subject, "The content of this email has not been provided yet", new ArrayList<>(), new ArrayList<>());
    }

    public void sendPhoneNumberIsWrongEmail(Policy policy) throws IOException, MessagingException {
        logger.info("Sending phone number is wrong email");
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), subject, "The content of this email has not been provided yet", new ArrayList<>(), new ArrayList<>());
    }

    public void sendEreceiptEmail(Policy policy, Pair<byte[], String> attachFile) throws IOException, MessagingException {
        logger.info("Sending ereceipt email");
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/logo.png")), "<imageElife>"));
        List<Pair<byte[], String>> fileList = new ArrayList<>();
        fileList.add(attachFile);
        String sbj = "";
        if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            sbj = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject-ifine.txt"), Charset.forName("UTF-8"));
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            sbj = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-subject-10ec.txt"), Charset.forName("UTF-8"));
        }
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), sbj, getEreceiptEmailContent(policy), base64ImgFileNames, fileList);
        logger.info("Ereceipt email sent");
    }

    private String getQuote10ECEmailContent(Quote quote) throws IOException {
        String decimalFormat = "#,##0.00";
        String emailContent = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-quote-10ec-content.txt"), Charset.forName("UTF-8"));
        return emailContent.replace("%1$s", quote.getCommonData().getNbOfYearsOfCoverage().toString())
                .replace("%2$s", quote.getCommonData().getNbOfYearsOfPremium().toString())
                .replace("%3$s", quote.getInsureds().get(0).getAgeAtSubscription().toString())
                .replace("%4$s", messageSource.getMessage("payment.mode." + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale))
                .replace("%5$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue()))
                .replace("%6$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                .replace("%7$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsMinimum().get(9).getValue()))
                .replace("%8$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsAverage().get(9).getValue() + quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksAverageBenefit().get(9).getValue()))
                .replace("%9$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsMaximum().get(9).getValue() + quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksMaximumBenefit().get(9).getValue()))
                .replace("%10$s", "'" + getLineURL() + "'")
                .replace("%11$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%12$s", "'" + getLineURL() + "quote-product/line-10-ec" + "'");
    }

    private String getQuoteiFineEmailContent(Quote quote) throws IOException {
        String decimalFormat = "#,##0.00";
        String emailContent = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-quote-ifine-content.txt"), Charset.forName("UTF-8"));
        ProductIFinePremium p = quote.getPremiumsData().getProductIFinePremium();
        return emailContent.replace("%2$s", getThaiDate(quote.getInsureds().get(0).getStartDate()))
                .replace("%3$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%4$s", String.valueOf(quote.getCommonData().getNbOfYearsOfCoverage()))
                .replace("%5$s", String.valueOf(quote.getCommonData().getNbOfYearsOfPremium()))
                .replace("%6$s", String.valueOf(quote.getInsureds().get(0).getAgeAtSubscription()))
                .replace("%7$s", messageSource.getMessage("payment.mode."+quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale))
                .replace("%8$s", (new DecimalFormat(decimalFormat)).format(p.getSumInsured().getValue()))
                .replace("%9$s", (new DecimalFormat(decimalFormat)).format(p.getAccidentSumInsured().getValue()))
                .replace("%10$s", (new DecimalFormat(decimalFormat)).format(p.getDeathByAccidentInPublicTransport().getValue()))
                .replace("%11$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfHandOrLeg().getValue()))
                .replace("%12$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfSight().getValue()))
                .replace("%13$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfHearingMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getLossOfHearingMax().getValue()))
                .replace("%14$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfSpeech().getValue()))
                .replace("%15$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfCorneaForBothEyes().getValue()))
                .replace("%16$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfFingersMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getLossOfFingersMax().getValue()))
                .replace("%17$s", (new DecimalFormat(decimalFormat)).format(p.getNoneCurableBoneFracture().getValue()))
                .replace("%18$s", (new DecimalFormat(decimalFormat)).format(p.getLegsShortenBy5cm().getValue()))
                .replace("%19$s", (new DecimalFormat(decimalFormat)).format(p.getBurnInjuryMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getBurnInjuryMax().getValue()))
                .replace("%20$s", (new DecimalFormat(decimalFormat)).format(p.getMedicalCareCost().getValue()))
                .replace("%21$s", (new DecimalFormat(decimalFormat)).format(p.getHospitalizationSumInsured().getValue()))
                .replace("%22$s", "'" + getLineURL() + "'")
                .replace("%23$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%24$s", "'" + getLineURL() + "quote-product/line-iFine" + "'");
    }

    private String getEreceiptEmailContent(Policy policy) throws IOException {
        String emailContent = IOUtils.toString(this.getClass().getResourceAsStream("/email-content/email-ereceipt-content.txt"), Charset.forName("UTF-8"));
        return emailContent.replace("%1$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName())
                .replace("%2$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName())
                .replace("%3$s", policy.getCommonData().getProductId() + " " + messageSource.getMessage("product.id." + policy.getCommonData().getProductId(), null, thLocale));
    }

    private String getLineURL() {
        return "https://line.me/R/ch/" + lineId + "/elife/th/";
    }

    private String getThaiDate(LocalDate localDate) {
        ThaiBuddhistDate tdate = ThaiBuddhistDate.from(localDate);
        return tdate.format(ofPattern("dd/MM/yyyy"));
    }
}
