package th.co.krungthaiaxa.api.elife.products;

import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author khoi.tran on 8/31/16.
 */
public abstract class BaseEmailService {
    private final static Logger logger = LoggerFactory.getLogger(BaseEmailService.class);
    //    public static final String EMAIL_PATH = "/email-content/email-quote-iprotect-content.txt";
    @Value("${email.name}")
    private String fromEmail;

    @Value("${email.subject.quote}")
    private String emailQuoteSubject;

    @Value("https://line.me/R/ch/${lineId}/elife/th/")
    private String lineURL;
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    private final EmailSender emailSender;
    private final IProtectSaleIllustrationService saleIllustrationService;

    @Inject
    public BaseEmailService(EmailSender emailSender, IProtectSaleIllustrationService saleIllustrationService) {
        this.emailSender = emailSender;
        this.saleIllustrationService = saleIllustrationService;
    }

    public void sendQuoteIProtect(Quote quote) {
        logger.info("Sending quote iProtect email...");
        String emailTemplate = IOUtil.loadTextFileInClassPath(getEmailTemplatePath());
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        try {
            attachments.add(saleIllustrationService.generatePDF(quote));
        } catch (DocumentException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        String emailContent = getEmailContent(emailTemplate, quote);
        emailSender.sendEmail(fromEmail, mainInsured.getPerson().getEmail(), emailQuoteSubject, emailContent, loadImagePairs(), attachments);
        logger.info("Quote iProtect email sent!");
    }

    protected List<Pair<byte[], String>> loadImagePairs() {
        List<Pair<byte[], String>> base64ImgFileNames = EmailUtil.initImagePairs("logo");
        return base64ImgFileNames;
    }

    private String getEmailContent(String emailTemplate, Quote quote) {
        String dcFormat = "#,##0.00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DecimalFormat dcf = new DecimalFormat(dcFormat);
        String emailContent = emailTemplate;
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        Integer taxDeclared = (mainInsured.getDeclaredTaxPercentAtSubscription() == null ? 0 : mainInsured.getDeclaredTaxPercentAtSubscription());
        return emailContent.replace("%1$s", quote.getCreationDateTime().plusYears(543).format(formatter))
                .replace("%2$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%3$s", dcf.format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                .replace("%4$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getDeathBenefit().getValue()))
                .replace("%5$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getSumInsured().getValue()))
                .replace("%6$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getYearlyTaxDeduction().getValue()))
                .replace("%7$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getTotalTaxDeduction().getValue()))
                .replace("%8$s", "'" + getLineURL() + "'")
                .replace("%9$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%10$s", "'" + getLineURL() + "quote-product/line-iProtect" + "'")
                .replace("%11$s", String.valueOf(taxDeclared))
                .replace("%12$s", messageSource.getMessage("payment.mode." + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale));
    }

    abstract protected String getEmailTemplatePath();

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getLineURL() {
        return lineURL;
    }

    public void setLineURL(String lineURL) {
        this.lineURL = lineURL;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

}
