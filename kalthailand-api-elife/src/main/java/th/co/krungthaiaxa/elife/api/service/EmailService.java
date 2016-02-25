package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.DatedAmount;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.utils.EmailSender;

import javax.inject.Inject;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.apache.commons.io.IOUtils.toByteArray;

@Service
public class EmailService {

    private final EmailSender emailSender;
    private final SaleIllustrationService saleIllustrationService;

    @Inject
    public EmailService(EmailSender emailSender, SaleIllustrationService saleIllustrationService) {
        this.emailSender = emailSender;
        this.saleIllustrationService = saleIllustrationService;
    }

    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);
    @Value("${email.smtp.server}")
    private String smtp;
    @Value("${email.name}")
    private String emailName;
    @Value("${email.subject}")
    private String subject;
    @Value("${lineid}")
    private String lineURL;
    @Value("${receipt.button.url}")
    private String uploadDocURL;
    @Value("${email.ereceipt.subject}")
    private String ereceiptSubject;

    public void sendQuoteEmail(Quote quote, String base64Image) throws Exception {
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(Base64.getDecoder().decode(base64Image), "<imageElife2>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitBlue.jpg")), "<benefitRed>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitGreen.jpg")), "<benefitGreen>"));
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/benefitPoint.jpg")), "<benefitPoint>"));
        //generate sale illustration pdf file
        List<File> Attachlist = (new ArrayList<File>());
        Attachlist.add(saleIllustrationService.generatePDF(quote, base64Image));
        emailSender.sendEmail(emailName, quote.getInsureds().get(0).getPerson().getEmail(), subject, getQuoteEmailContent(quote), base64ImgFileNames, Attachlist);
    }

    public void sendEreceiptEmail(Policy policy, String attachFile) throws Exception {
        List<Pair<byte[], String>> base64ImgFileNames = new ArrayList<>();
        base64ImgFileNames.add(Pair.of(toByteArray(this.getClass().getResourceAsStream("/images/email/logo.png")), "<imageElife>"));
        List<File> fileList = new ArrayList<>();
        fileList.add(new File(attachFile));
        emailSender.sendEmail(emailName, policy.getInsureds().get(0).getPerson().getEmail(), subject, getEreceiptEmailContent(policy), base64ImgFileNames, fileList);
    }

    private String getQuoteEmailContent(Quote quote) {
        String decimalFormat = "#,##0.00";

        File file = new File((getClass().getClassLoader()).getResource("email-quote-content.txt").getFile());

        String emailContent = "";
        try {
            List lines = Files.readAllLines(Paths.get(file.getAbsolutePath()),
                    Charset.defaultCharset());
            for (Object line : lines) {
                emailContent += (String) line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emailContent.replace("%1$s", quote.getCommonData().getNbOfYearsOfCoverage().toString())
                .replace("%2$s", quote.getCommonData().getNbOfYearsOfPremium().toString())
                .replace("%3$s", quote.getInsureds().get(0).getAgeAtSubscription().toString())
                .replace("%4$s", quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString())
                .replace("%5$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsurance().getSumInsured().getValue()))
                .replace("%6$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                .replace("%7$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMinimum().stream().mapToDouble(DatedAmount::getValue).sum()))
                .replace("%8$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsAverage().stream().mapToDouble(DatedAmount::getValue).sum()))
                .replace("%9$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMaximum().stream().mapToDouble(DatedAmount::getValue).sum()))
                .replace("%10$s", "/" + lineURL + "/")
                .replace("%11$s", "/" + lineURL + "fatca-questions/" + quote.getQuoteId() + "/")
                .replace("%12$s", "/" + lineURL + "quote-product/line-10-ec" + "/");
    }

    private String getEreceiptEmailContent(Policy policy) {
        File file = new File((getClass().getClassLoader()).getResource("email-ereceipt-content.txt").getFile());
        String emailContent = "";
        try {
            List lines = Files.readAllLines(Paths.get(file.getAbsolutePath()),
                    Charset.defaultCharset());
            for (Object line : lines) {
                emailContent += (String) line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emailContent.replace("%1$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName())
                .replace("%2$s", policy.getInsureds().get(0).getPerson().getGivenName() + " " + policy.getInsureds().get(0).getPerson().getSurName())
                .replace("%3$s", "/" + uploadDocURL + "/");
    }

}
