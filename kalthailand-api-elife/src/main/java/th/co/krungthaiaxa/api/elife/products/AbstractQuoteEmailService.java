package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailHelper;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;
import th.co.krungthaiaxa.api.elife.service.SigningDocumentService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * @author khoi.tran on 8/31/16.
 */
public abstract class AbstractQuoteEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQuoteEmailService.class);

    @Value("${email.subject.quote}")
    private String emailQuoteSubject;

    @Value("https://line.me/R/ch/${line.app.id}/elife/th/")
    private String lineURL;

    private final ElifeEmailService axaEmailService;
    private final ElifeEmailHelper axaEmailHelper;
    private final SaleIllustrationService saleIllustrationService;
    private final SigningDocumentService signingDocumentService;

    @Inject
    public AbstractQuoteEmailService(ElifeEmailService axaEmailService, ElifeEmailHelper axaEmailHelper, SaleIllustrationService saleIllustrationService, SigningDocumentService signingDocumentService) {
        this.axaEmailService = axaEmailService;
        this.axaEmailHelper = axaEmailHelper;
        this.saleIllustrationService = saleIllustrationService;
        this.signingDocumentService = signingDocumentService;
    }

    public void sendQuoteEmail(Quote quote, String accessToken) {
        LOGGER.info("Sending quote iProtect email...");
        String emailTemplate = IOUtil.loadTextFileInClassPath(getEmailTemplatePath(quote));
        Pair<byte[], String> rawAttachment = saleIllustrationService.generatePDF(quote);
        byte[] signedPdf = signingDocumentService.signPDFFile(rawAttachment.getLeft(), accessToken);
        List<Pair<byte[], String>> attachments = Collections.singletonList(Pair.of(signedPdf, rawAttachment.getRight()));

        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        String emailContent = getEmailContent(emailTemplate, quote);
        axaEmailService.sendEmail(mainInsured.getPerson().getEmail(), emailQuoteSubject, emailContent, attachments);
        LOGGER.info("Quote iProtect email sent!");
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

    public ElifeEmailService getAxaEmailService() {
        return axaEmailService;
    }

    public ElifeEmailHelper getAxaEmailHelper() {
        return axaEmailHelper;
    }
}
