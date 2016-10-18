package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.service.AxaEmailHelper;
import th.co.krungthaiaxa.api.elife.service.AxaEmailService;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 8/31/16.
 */
public abstract class AbstractQuoteEmailService {
    private final static Logger logger = LoggerFactory.getLogger(AbstractQuoteEmailService.class);

    @Value("${email.subject.quote}")
    private String emailQuoteSubject;

    @Value("https://line.me/R/ch/${line.app.id}/elife/th/")
    private String lineURL;

    private final AxaEmailService axaEmailService;
    private final AxaEmailHelper axaEmailHelper;
    private final SaleIllustrationService saleIllustrationService;

    @Inject
    public AbstractQuoteEmailService(AxaEmailService axaEmailService, AxaEmailHelper axaEmailHelper, SaleIllustrationService saleIllustrationService) {
        this.axaEmailService = axaEmailService;
        this.axaEmailHelper = axaEmailHelper;
        this.saleIllustrationService = saleIllustrationService;
    }

    public void sendQuoteEmail(Quote quote) {
        logger.info("Sending quote iProtect email...");
        String emailTemplate = IOUtil.loadTextFileInClassPath(getEmailTemplatePath(quote));
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(saleIllustrationService.generatePDF(quote));
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        String emailContent = getEmailContent(emailTemplate, quote);
        axaEmailService.sendEmail(mainInsured.getPerson().getEmail(), emailQuoteSubject, emailContent, loadImagePairs(), attachments);
        logger.info("Quote iProtect email sent!");
    }

    protected List<Pair<byte[], String>> loadImagePairs() {
        List<Pair<byte[], String>> base64ImgFileNames = EmailUtil.initImagePairs("logo");
        return base64ImgFileNames;
    }

    protected String getEmailTemplatePath(Quote quote) {
        String productId = quote.getCommonData().getProductId();
        return String.format("/products/%s/email-quote.html", productId.toLowerCase());
    }

    abstract protected String getEmailContent(String emailTemplate, Quote quote);

    protected Double getVal(Amount amount) {
        return amount.getValue();
    }

    public String getLineURL() {
        return lineURL;
    }

    public void setLineURL(String lineURL) {
        this.lineURL = lineURL;
    }

    public AxaEmailService getAxaEmailService() {
        return axaEmailService;
    }

    public AxaEmailHelper getAxaEmailHelper() {
        return axaEmailHelper;
    }
}
