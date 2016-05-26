package th.co.krungthaiaxa.api.elife.service;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Person;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

@Service
public class DAFormService {

    private final static Logger logger = LoggerFactory.getLogger(DAFormService.class);

    private final Color FONT_COLOR = Color.BLACK;

    public byte[] generateDAForm(Policy pol) throws Exception {
        logger.info(String.format("[%1$s] ...", "generateDAForm"));
        logger.info(String.format("policy is %1$s ...", pol.getPolicyId()));

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, content);
        document.open();

        // page content
        PdfContentByte canvas1 = writer.getDirectContentUnder();
        canvas1.addImage(getPdfImage(getPageContent(pol)));

        document.close();
        content.close();
        return content.toByteArray();
    }

    private Image getPdfImage(byte[] imageConent) throws IOException, BadElementException {
        Image image = Image.getInstance(imageConent);
        image.scaleAbsolute(PageSize.A4);
        image.setAbsolutePosition(0, 0);
        return image;
    }

    private byte[] getPageContent(Policy pol) throws Exception {
        InputStream is1 = getClass().getClassLoader().getResourceAsStream("da-form/DA_FORM_20150311.jpg");
        BufferedImage bf1 = ImageIO.read(is1);
        Graphics g1 = bf1.getGraphics();
        g1 = setGraphicColorAndFont(g1);

        Insured insured = pol.getInsureds().get(0);
        Person person = insured.getPerson();

        ThaiBuddhistDate thaiDateOfNow = ThaiBuddhistDate.from(LocalDate.now());

        g1.drawString(thaiDateOfNow.format(ofPattern("dd")), 1620, 535);

        g1.drawString(thaiDateOfNow.format(ofPattern("MMMM", new Locale("th", "TH"))), 1885, 535);

        g1.drawString(thaiDateOfNow.format(ofPattern("yyyy")), 2240, 535);

        g1.drawString(person.getGivenName() + " " + person.getSurName(), 530, 695);

        g1.drawString(person.getRegistrations().get(0).getId(), 1870, 695);

        if (person.getHomePhoneNumber() != null && person.getHomePhoneNumber().getNumber() != null) {
            g1.drawString(person.getHomePhoneNumber().getNumber(), 290, 785);
        }

        if (person.getWorkPhoneNumber() != null && person.getWorkPhoneNumber().getNumber() != null) {
            g1.drawString(person.getWorkPhoneNumber().getNumber(), 690, 785);
        }

        if (person.getMobilePhoneNumber() != null && person.getMobilePhoneNumber().getNumber() != null) {
            g1.drawString(person.getMobilePhoneNumber().getNumber(), 1080, 785);
        }

        g1.drawString(person.getEmail(), 1440, 785);

        char[] numberPNO = pol.getPolicyId().toCharArray();
        g1.drawString(String.valueOf(numberPNO[0]), 415, 935);
        g1.drawString(String.valueOf(numberPNO[1]), 475, 935);
        g1.drawString(String.valueOf(numberPNO[2]), 535, 935);
        g1.drawString(String.valueOf(numberPNO[4]), 620, 935);
        g1.drawString(String.valueOf(numberPNO[5]), 680, 935);
        g1.drawString(String.valueOf(numberPNO[6]), 735, 935);
        g1.drawString(String.valueOf(numberPNO[7]), 790, 935);
        g1.drawString(String.valueOf(numberPNO[8]), 850, 935);
        g1.drawString(String.valueOf(numberPNO[9]), 910, 935);
        g1.drawString(String.valueOf(numberPNO[10]), 970, 935);

        g1.drawString(person.getGivenName() + " " + person.getSurName(), 1340, 940);

        g1.drawString("ผู้เอาประกัน", 2175, 940);

        g1.drawString("111", 930, 1380);

        g1.drawString("000", 1770, 1380);

        g1.drawString("LINE PAY", 245, 1475);

        g1.drawString("0", 1395, 1470);
        g1.drawString("0", 1470, 1470);
        g1.drawString("0", 1545, 1470);
        g1.drawString("0", 1635, 1470);
        g1.drawString("0", 1725, 1470);
        g1.drawString("0", 1805, 1470);
        g1.drawString("0", 1880, 1470);
        g1.drawString("0", 1955, 1470);
        g1.drawString("0", 2030, 1470);
        g1.drawString("0", 2125, 1470);

        return getImageBytes(bf1);
    }

    private byte[] getImageBytes(BufferedImage bf1) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bf1, "jpg", baos);
        return baos.toByteArray();
    }

    private Graphics setGraphicColorAndFont(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(50f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

    private Graphics setGraphicColorAndFontBigText(Graphics g) throws IOException {
        g.setColor(FONT_COLOR);
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("ereceipt/ANGSAB_1.TTF")).deriveFont(100f);
            g.setFont(f);
        } catch (FontFormatException e) {
            logger.error("Unable to load embed font file", e);
            throw new IOException(e);
        }
        return g;
    }

}
