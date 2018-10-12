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
            pdfReader.selectPages("1");
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

        String citizenIds [] =  person.getRegistrations().get(0).getId().split("");
        ThaiBuddhistDate thaiDateOfNow = ThaiBuddhistDate.from(LocalDate.now());

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("dd")), 342, 694, MEDIUM_SIZE); //Y = 716 X = 392

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("MMMM", new Locale("th", "TH"))), 404, 694, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, thaiDateOfNow.format(ofPattern("yyyy")), 492, 694, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, person.getGivenName() + " " + person.getSurName(), 150, 666, MEDIUM_SIZE);
               
        if(citizenIds != null && citizenIds.length == 13) {        	
        	PdfUtil.writeText(page, baseFont, citizenIds[0], 417, 666, MEDIUM_SIZE);    
        	PdfUtil.writeText(page, baseFont, citizenIds[1], 429, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[2], 438, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[3], 447, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[4], 457, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[5], 470, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[6], 480, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[7], 489, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[8], 499, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[9], 508, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[10], 520, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[11], 529, 666, MEDIUM_SIZE);  
        	PdfUtil.writeText(page, baseFont, citizenIds[12], 541, 666, MEDIUM_SIZE);  
        }               

        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getHomePhoneNumber().getNumber(), 110, 651, MEDIUM_SIZE);
        }

        if (person.getWorkPhoneNumber() != null && person.getWorkPhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getWorkPhoneNumber().getNumber(), 224, 651, MEDIUM_SIZE);
        }

        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            PdfUtil.writeText(page, baseFont, person.getMobilePhoneNumber().getNumber(), 350, 651, MEDIUM_SIZE);
        }

        PdfUtil.writeText(page, baseFont, person.getEmail(), 435, 651, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, policy.getPolicyId(), 160, 622, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, person.getGivenName() + " " + person.getSurName(), 317, 622, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "ผู้เอาประกัน", 480, 622, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "111", 231, 540, MEDIUM_SIZE); 
        PdfUtil.writeText(page, baseFont, "000", 430, 540, MEDIUM_SIZE);

        PdfUtil.writeText(page, baseFont, "LINE PAY", 90, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 323, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 334, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 346, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 361, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 380, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 392, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 402, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 414, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 426, 520, MEDIUM_SIZE);
        PdfUtil.writeText(page, baseFont, "0", 442, 520, MEDIUM_SIZE);

    }
}
