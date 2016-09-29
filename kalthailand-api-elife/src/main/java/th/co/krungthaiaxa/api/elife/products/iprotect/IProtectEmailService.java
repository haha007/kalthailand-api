package th.co.krungthaiaxa.api.elife.products.iprotect;

import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
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
@Service
public class IProtectEmailService {
    private final static Logger logger = LoggerFactory.getLogger(IProtectEmailService.class);
    public static final String EMAIL_PATH = "/email-content/email-quote-iprotect-content.txt";
    @Value("${email.name}")
    private String fromEmail;
    @Value("${email.subject.quote}")
    private String emailQuoteSubject;
    @Value("${line.app.id}")
    private String lineId;
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");

    private final EmailSender emailSender;
    private final IProtectSaleIllustrationService iProtectSaleIllustrationService;

    @Inject
    public IProtectEmailService(EmailSender emailSender, IProtectSaleIllustrationService iProtectSaleIllustrationService) {
        this.emailSender = emailSender;
        this.iProtectSaleIllustrationService = iProtectSaleIllustrationService;
    }

    public void sendQuoteIProtect(Quote quote) {
        logger.info("Sending quote iProtect email...");
        List<Pair<byte[], String>> base64ImgFileNames = EmailUtil.initImagePairs("logo");
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        try {
            attachments.add(iProtectSaleIllustrationService.generatePDF(quote));
        } catch (DocumentException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        emailSender.sendEmail(fromEmail, mainInsured.getPerson().getEmail(), emailQuoteSubject, getEmailContent(quote), base64ImgFileNames, attachments);
        logger.info("Quote iProtect email sent!");
    }

    private String getEmailContent(Quote quote) {
        String dcFormat = "#,##0.00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DecimalFormat dcf = new DecimalFormat(dcFormat);
        String emailContent = IOUtil.loadTextFileInClassPath(EMAIL_PATH);
        Integer taxDeclared = (quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription() == null ? 0 : quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription());
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

    private String getLineURL() {
        return "https://line.me/R/ch/" + lineId + "/elife/th/";
    }
}
