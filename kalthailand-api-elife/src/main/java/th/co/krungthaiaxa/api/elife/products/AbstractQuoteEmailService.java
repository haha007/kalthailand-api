package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author khoi.tran on 8/31/16.
 */
public abstract class AbstractQuoteEmailService {
    private final static Logger logger = LoggerFactory.getLogger(AbstractQuoteEmailService.class);
    //    public static final String EMAIL_PATH = "/email-content/email-quote-iprotect-content.txt";
    @Value("${email.name}")
    private String fromEmail;

    @Value("${email.subject.quote}")
    private String emailQuoteSubject;

    @Value("https://line.me/R/ch/${line.app.id}/elife/th/")
    private String lineURL;
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    private final static String DECIMAL_PATTERN = "#,##0.00";
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(DECIMAL_PATTERN);
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateTimeUtil.PATTERN_THAI_DATE);

    private final EmailSender emailSender;
    private final SaleIllustrationService saleIllustrationService;

    @Inject
    public AbstractQuoteEmailService(EmailSender emailSender, SaleIllustrationService saleIllustrationService) {
        this.emailSender = emailSender;
        this.saleIllustrationService = saleIllustrationService;
    }

    public void sendQuoteEmail(Quote quote) {
        logger.info("Sending quote iProtect email...");
        String emailTemplate = IOUtil.loadTextFileInClassPath(getEmailTemplatePath(quote));
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(saleIllustrationService.generatePDF(quote));
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        String emailContent = getEmailContent(emailTemplate, quote);
        emailSender.sendEmail(fromEmail, mainInsured.getPerson().getEmail(), emailQuoteSubject, emailContent, loadImagePairs(), attachments);
        logger.info("Quote iProtect email sent!");
    }

    protected List<Pair<byte[], String>> loadImagePairs() {
        List<Pair<byte[], String>> base64ImgFileNames = EmailUtil.initImagePairs("logo");
        return base64ImgFileNames;
    }

    protected String getEmailTemplatePath(Quote quote) {
        String productId = quote.getCommonData().getProductId();
        return String.format("/products/%s/quote-email-template.html", productId);
    }

    protected String toCurrency(Double value) {
        return DECIMAL_FORMAT.format(value);
    }

    protected String toThaiYear(LocalDateTime time) {
        return time.plusYears(543).format(DATE_TIME_FORMATTER);
    }

    protected String toThaiPaymentMode(Periodicity due) {
        return messageSource.getMessage("payment.mode." + due.getCode().toString(), null, thLocale);
    }

    abstract protected String getEmailContent(String emailTemplate, Quote quote);

    protected Double getVal(Amount amount) {
        return amount.getValue();
    }

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
