package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.ProductIFinePremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 8/31/16.
 */
@Service
public class IProtectEmailService {
    private final static Logger logger = LoggerFactory.getLogger(IProtectEmailService.class);
    public static final String EMAIL_PATH = "/email-content/email-quote-iprotect-content.html";
    @Value("${email.subject.quote}")
    private String emailQuoteSubject;

    private final EmailService emailService;
    private final IProtectSaleIllustrationService iProtectSaleIllustrationService;

    @Inject
    public IProtectEmailService(EmailService emailService, IProtectSaleIllustrationService iProtectSaleIllustrationService) {
        this.emailService = emailService;
        this.iProtectSaleIllustrationService = iProtectSaleIllustrationService;
    }

    public void sendQuoteIProtect(Quote quote) {
        logger.info("Sending quote iProtect email...");
        List<Pair<byte[], String>> base64ImgFileNames = EmailUtil.getDefaultImagePairs();
        List<Pair<byte[], String>> attachments = new ArrayList<>();
        attachments.add(iProtectSaleIllustrationService.generatePDF(quote));
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        emailService.sendEmail(mainInsured.getPerson().getEmail(), emailQuoteSubject, getEmailContent(quote), base64ImgFileNames, attachments);
        logger.info("Quote iProtect email sent!");
    }

    private String getEmailContent(Quote quote) {
        String decimalFormat = "#,##0.00";
        String emailContent = IOUtil.loadTextFileInClassPath(EMAIL_PATH);
        ProductIFinePremium p = quote.getPremiumsData().getProductIFinePremium();
        return emailContent.replace("%2$s", DateTimeUtil.formatThaiDateTime(quote.getCreationDateTime()))
                //TODO need to continue
//                .replace("%3$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
//                .replace("%4$s", String.valueOf(quote.getCommonData().getNbOfYearsOfCoverage()))
//                .replace("%5$s", String.valueOf(quote.getCommonData().getNbOfYearsOfPremium()))
//                .replace("%6$s", String.valueOf(quote.getInsureds().get(0).getAgeAtSubscription()))
//                .replace("%7$s", messageSource.getMessage("payment.mode." + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale))
//                .replace("%8$s", (new DecimalFormat(decimalFormat)).format(p.getSumInsured().getValue()))
//                .replace("%9$s", (new DecimalFormat(decimalFormat)).format(p.getAccidentSumInsured().getValue()))
//                .replace("%10$s", (new DecimalFormat(decimalFormat)).format(p.getDeathByAccidentInPublicTransport().getValue()))
//                .replace("%11$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfHandOrLeg().getValue()))
//                .replace("%12$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfSight().getValue()))
//                .replace("%13$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfHearingMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getLossOfHearingMax().getValue()))
//                .replace("%14$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfSpeech().getValue()))
//                .replace("%15$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfCorneaForBothEyes().getValue()))
//                .replace("%16$s", (new DecimalFormat(decimalFormat)).format(p.getLossOfFingersMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getLossOfFingersMax().getValue()))
//                .replace("%17$s", (new DecimalFormat(decimalFormat)).format(p.getNoneCurableBoneFracture().getValue()))
//                .replace("%18$s", (new DecimalFormat(decimalFormat)).format(p.getLegsShortenBy5cm().getValue()))
//                .replace("%19$s", (new DecimalFormat(decimalFormat)).format(p.getBurnInjuryMin().getValue()) + " - " + (new DecimalFormat(decimalFormat)).format(p.getBurnInjuryMax().getValue()))
//                .replace("%20$s", (new DecimalFormat(decimalFormat)).format(p.getMedicalCareCost().getValue()))
//                .replace("%21$s", (new DecimalFormat(decimalFormat)).format(p.getHospitalizationSumInsured().getValue()))
//                .replace("%22$s", "'" + getLineURL() + "'")
//                .replace("%23$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
//                .replace("%24$s", "'" + getLineURL() + "quote-product/line-iFine" + "'")
                ;
    }
}
