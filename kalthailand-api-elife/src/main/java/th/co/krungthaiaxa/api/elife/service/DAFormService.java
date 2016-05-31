package th.co.krungthaiaxa.api.elife.service;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Policy;

@Service
public class DAFormService {
	private final static Logger logger = LoggerFactory.getLogger(DAFormService.class);
    private static final float VERY_SMALL_SIZE = 7f;
    private static final float SMALL_SIZE = 10f;
    private static final float MEDIUM_SIZE = 13f;    
    private static final float BIG_SIZE = 25f;
    
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");
    
    private final String MARK = "X";
    private final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    
    public byte[] generateDAForm(Policy pol) throws Exception {
    	
    	ByteArrayOutputStream content = new ByteArrayOutputStream();   
    	
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("da-form/da-form-empty.pdf");
        PdfReader pdfReader = new PdfReader(inputStream);
        PdfStamper pdfStamper = new PdfStamper(pdfReader, content);
        
        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();
        
        BaseFont font = getBaseFont();
        
        ThaiBuddhistDate thaiDateOfNow = ThaiBuddhistDate.from(LocalDate.now());
        
        writeText(pdfStamper.getOverContent(1), font, thaiDateOfNow.format(ofPattern("dd")), 392, 716, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, thaiDateOfNow.format(ofPattern("MMMM", new Locale("th", "TH"))), 454, 716, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, thaiDateOfNow.format(ofPattern("yyyy")), 542, 716, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, person.getGivenName() + " " + person.getSurName(), 130, 676, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, person.getRegistrations().get(0).getId(), 454, 676, MEDIUM_SIZE);
        
        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
        	writeText(pdfStamper.getOverContent(1), font, person.getHomePhoneNumber().getNumber(), 68, 654, MEDIUM_SIZE);
        }
        
        if (person.getWorkPhoneNumber() != null && person.getWorkPhoneNumber().getNumber() != null) {
        	writeText(pdfStamper.getOverContent(1), font, person.getWorkPhoneNumber().getNumber(), 170, 654, MEDIUM_SIZE);
        }

        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
        	writeText(pdfStamper.getOverContent(1), font, person.getMobilePhoneNumber().getNumber(), 262, 654, MEDIUM_SIZE);
        }
        
        writeText(pdfStamper.getOverContent(1), font, person.getEmail(), 350, 654, MEDIUM_SIZE);
        
        char[] numberPNO = pol.getPolicyId().toCharArray();
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[0]), 100, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[1]), 112, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[2]), 128, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[4]), 148, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[5]), 162, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[6]), 178, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[7]), 192, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[8]), 206, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[9]), 222, 618, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, String.valueOf(numberPNO[10]), 236, 618, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, person.getGivenName() + " " + person.getSurName(), 328, 616, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, "ผู้เอาประกัน", 528, 616, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, "111", 226, 512, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, "000", 430, 512, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, "LINE PAY", 60, 488, MEDIUM_SIZE);
        
        writeText(pdfStamper.getOverContent(1), font, "0", 338, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 356, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 374, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 394, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 418, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 436, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 454, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 472, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 490, 488, MEDIUM_SIZE);
        writeText(pdfStamper.getOverContent(1), font, "0", 512, 488, MEDIUM_SIZE);
        
        
        
        
        pdfStamper.close();
        content.close();
        return content.toByteArray();
    }
    
    private BaseFont getBaseFont() throws IOException {
        BaseFont baseFont;
        try {
            byte[] bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF"));
            baseFont = BaseFont.createFont("ANGSAB_1.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        } catch (DocumentException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return baseFont;
    }
    
    private void writeText(PdfContentByte pdfContentByte, BaseFont font, String text, int x, int y, float fontSize) {
        pdfContentByte.beginText();
        pdfContentByte.setFontAndSize(font, fontSize);
        pdfContentByte.setTextMatrix(x, y);
        pdfContentByte.showText(text);
        pdfContentByte.endText();
    }

}
