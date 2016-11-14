package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.utils.PdfUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * When policy status is {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus#PENDING_PAYMENT}, you can change its status to {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus#PENDING_VALIDATION}, and when changing the status, DAForm will be generated.
 * <p>
 * 2016-11-09: v.1.10.0:
 * Only generated DAForm for policy with {@link th.co.krungthaiaxa.api.elife.model.enums.AtpMode#AUTOPAY}. Don't care about paymentMode is {@link th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode} is Month, Year,....
 * However, with {@link th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode#EVERY_MONTH}, the atpMode is always enabled (true).
 */
@Service
public class DAFormService {

    public final static Logger LOGGER = LoggerFactory.getLogger(DAFormService.class);
    public static final String DA_FORM_PDF_PATH = "da-form/DAForm_A4.pdf";
    private static final float MEDIUM_SIZE = 13f;
    private final BaseFont baseFont = PdfUtil.loadBaseFont();

    public byte[] generateDAFormPdf(Policy policy) {
        try (ByteArrayOutputStream content = new ByteArrayOutputStream()) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DA_FORM_PDF_PATH);
            PdfReader pdfReader = new PdfReader(inputStream);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, content);
            fillData(pdfStamper.getOverContent(1), policy);
            pdfStamper.close();
            content.close();
            return content.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new FileIOException(String.format("Generate eReceipt pdf [error]: \n\t policyId: %s. Error: %s", policy.getPolicyId(), e.getMessage()), e);
        }
    }

    private void fillData(PdfContentByte page, Policy policy) {

        Insured insured = ProductUtils.validateExistMainInsured(policy);
        Person person = insured.getPerson();

        ThaiBuddhistDate thaiDateOfNow = ThaiBuddhistDate.from(LocalDate.now());

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("dd")), 392, 716, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("MMMM", new Locale("th", "TH"))), 454, 716, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("yyyy")), 542, 716, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, person.getGivenName() + " " + person.getSurName(), 130, 676, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, person.getRegistrations().get(0).getId(), 454, 676, MEDIUM_SIZE);

        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getHomePhoneNumber().getNumber(), 70, 654, MEDIUM_SIZE);
        }

        if (person.getWorkPhoneNumber() != null && person.getWorkPhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getWorkPhoneNumber().getNumber(), 199, 654, MEDIUM_SIZE);
        }

        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getMobilePhoneNumber().getNumber(), 326, 654, MEDIUM_SIZE);
        }

        PdfUtil.writeText(page, baseFont, person.getEmail(), 452, 654, MEDIUM_SIZE);

        char[] numberPNO = policy.getPolicyId().toCharArray();
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[0]), 100, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[1]), 112, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[2]), 128, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[4]), 148, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[5]), 162, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[6]), 178, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[7]), 192, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[8]), 206, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[9]), 222, 618, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, String.valueOf(numberPNO[10]), 236, 618, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, person.getGivenName() + " " + person.getSurName(), 328, 616, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "ผู้เอาประกัน", 528, 616, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "111", 226, 512, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "000", 430, 512, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "LINE PAY", 60, 488, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "0", 338, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 356, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 374, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 394, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 418, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 436, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 454, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 472, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 490, 488, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 512, 488, MEDIUM_SIZE);

    }
}
