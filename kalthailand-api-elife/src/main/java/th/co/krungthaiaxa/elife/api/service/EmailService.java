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
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.products.ProductType;
import th.co.krungthaiaxa.elife.api.utils.EmailSender;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.io.IOUtils.toByteArray;

@Service
public class EmailService {
    private final static Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final EmailSender emailSender;
    private final SaleIllustration10ECService saleIllustration10ECService;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject.quote}")
    private String subject;
    @Value("${line.app.id}")
    private String lineId;
    @Value("${button.url.ereceipt.mail}")
    private String uploadDocURL;
    @Value("${email.subject.ereceipt.10ec}")
    private String emailSubjectFor10EC;
    @Value("${email.subject.ereceipt.ifine}")
    private String emailSubjectForIFine;

    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    @Inject
    public EmailService(EmailSender emailSender, SaleIllustration10ECService saleIllustration10ECService) {
        this.emailSender = emailSender;
        this.saleIllustration10ECService = saleIllustration10ECService;
    }

    public void sendQuoteEmail(Quote quote, String base64Image) throws IOException, MessagingException, DocumentException {
        logger.info("Sending quote email");
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(Base64.getDecoder().decode(base64Image), "<imageElife2>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitBlue.jpg")), "<benefitRed>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitGreen.jpg")), "<benefitGreen>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitPoint.jpg")), "<benefitPoint>"));
        //generate sale illustration pdf file
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(saleIllustration10ECService.generatePDF(quote, base64Image));
        emailSender.sendEmail(emailName, quote.getInsureds().get(0).getPerson().getEmail(), subject, getQuoteEmailContent(quote), base64ImgFileNames, attachments);
        logger.info("Quote email sent");
    }

    public void sendEreceiptEmail(Policy policy, Pair<byte[], String> attachFile) throws IOException, MessagingException {
        logger.info("Sending ereceipt email");
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/logo.png")), "<imageElife>"));
        List<Pair<byte[], String>> fileList = new ArrayList<>();
        fileList.add(attachFile);
        String sbj = "";
        if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_IFINE.getName())) {
            sbj = emailSubjectForIFine;
        } else if (policy.getCommonData().getProductId().equals(ProductType.PRODUCT_10_EC.getName())) {
            sbj = emailSubjectFor10EC;
        }
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), sbj, getEreceiptEmailContent(policy), base64ImgFileNames, fileList);
        logger.info("Ereceipt email sent");
    }

    private String getQuoteEmailContent(Quote quote) throws IOException {
        String decimalFormat = "#,##0.00";
        String emailContent = IOUtils.toString(this.getClass().getResourceAsStream("/email-quote-content.txt"));
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

    private String getEreceiptEmailContent(Policy policy) throws IOException {
        String emailContent = IOUtils.toString(this.getClass().getResourceAsStream("/email-ereceipt-content.txt"));
        return emailContent.replace("%1$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName())
                .replace("%2$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName());
    }

    private String getLineURL() {
        return "https://line.me/R/ch/" + lineId + "/elife/th/";
    }

}
