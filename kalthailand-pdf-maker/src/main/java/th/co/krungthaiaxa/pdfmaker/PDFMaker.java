package th.co.krungthaiaxa.pdfmaker;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PDFMaker {
    private static final Logger logger = LoggerFactory.getLogger(PDFMaker.class);

    public void start() {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer;
        try {
            writer = PdfWriter.getInstance(document, content);
        } catch (DocumentException e) {
            logger.error("Unable to create empty PDF", e);
            return;
        }
        document.open();

        // page1
        try {
            PdfContentByte canvas1 = writer.getDirectContentUnder();
            canvas1.addImage(getPdfImage(getPage("application-form-1.gif")));
        } catch (DocumentException | IOException e) {
            logger.error("Unable to add page 1", e);
            return;
        }

        // page2
        document.newPage();
        try {
            PdfContentByte canvas2 = writer.getDirectContentUnder();
            canvas2.addImage(getPdfImage(getPage("application-form-2.gif")));
        } catch (DocumentException | IOException e) {
            logger.error("Unable to add page 2", e);
            return;
        }

        // page3
        document.newPage();
        try {
            PdfContentByte canvas3 = writer.getDirectContentUnder();
            canvas3.addImage(getPdfImage(getPage("application-form-3.gif")));
        } catch (DocumentException | IOException e) {
            logger.error("Unable to add page 3", e);
            return;
        }

        document.close();
        try {
            content.close();
        } catch (IOException e) {
            logger.error("Unable to close document", e);
            return;
        }

        try {
            FileUtils.writeByteArrayToFile(new File("application-form.pdf"), content.toByteArray());
        } catch (IOException e) {
            logger.error("Unable to write final pdf document", e);
        }
    }

    private Image getPdfImage(byte[] imageConent) throws IOException, BadElementException {
        Image image = Image.getInstance(imageConent);
        image.scaleAbsolute(PageSize.A4);
        image.setAbsolutePosition(0, 0);
        return image;
    }

    private byte[] getPage(String fileName) throws IOException {
        InputStream is3 = getClass().getClassLoader().getResourceAsStream(fileName);
        BufferedImage bf3 = ImageIO.read(is3);

        return getImageBytes(bf3);
    }

    private byte[] getImageBytes(BufferedImage bf1) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 1024);
        ImageIO.write(bf1, "png", baos);
        return baos.toByteArray();
    }

}
