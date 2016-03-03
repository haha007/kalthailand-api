package th.co.krungthaiaxa.elife.api.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    private final static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    public static void imageToPDF(byte[] bytesImage, String outputPDFFile) throws IOException,DocumentException {
        logger.info("[imageToPDF] begin");
        Document document = new Document();
        Integer indentation = 0;
        try (FileOutputStream fos = new FileOutputStream(outputPDFFile)){
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            writer.open();
            document.open();
            Image image = Image.getInstance(bytesImage);
            float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            document.add(image);
            document.close();
            writer.close();
        } catch (IOException e) {
            logger.error("Unable Create PDF from output file", e);
            throw e;
        } catch(DocumentException e){
            logger.error("Unable Create PDF from itext", e);
            throw e;
        }
        logger.info("[imageToPDF] imageToPDF : Convert completed.");
    }
}
