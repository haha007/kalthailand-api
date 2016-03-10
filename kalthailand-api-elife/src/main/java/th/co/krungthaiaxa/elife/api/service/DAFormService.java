package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * Created by santilik on 3/10/2016.
 */
public class DAFormService {

    private final static Logger logger = LoggerFactory.getLogger(DAFormService.class);

    private final String DA_IMAGE = "DA_FORM_LINEPAY_260159.jpg";
    private final String FONT_NAME = "Angsana New";
    private final Integer FONT_SIZE = 50;
    private final Integer FONT_SIZE_BIG = 100;
    private final Color FONT_COLOR = Color.BLACK;
    private final String MARK = "X";
    private final String ID_CARD_DOC = "Thai ID Card number";
    private final Integer INDX = 0;
    private final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public void generateDAForm()throws Exception{

        InputStream is1 = getClass().getClassLoader().getResourceAsStream("da-form/" + DA_IMAGE);
        BufferedImage bf1 = ImageIO.read(is1);
        Graphics g1 = bf1.getGraphics();
        g1 = setGraphicColorAndFontBigText(g1);

        //Policy number
        g1.drawString("12", 1615, 535);

        //generate page1
        File outputfile1 = new File("D:\\test\\da-form-marge.jpg");
        ImageIO.write(bf1, "jpg", outputfile1);

        //create pdf file
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("D:\\test\\da-form-marge.pdf"));
        document.open();

        //merge page1
        PdfContentByte canvas1 = writer.getDirectContentUnder();
        com.itextpdf.text.Image image1 = com.itextpdf.text.Image.getInstance("D:\\test\\da-form-marge.jpg");
        image1.scaleAbsolute(PageSize.A4);
        image1.setAbsolutePosition(0, 0);
        canvas1.addImage(image1);

        document.close();

    }

    private Graphics setGraphicColorAndFontBigText(Graphics g){
        g.setColor(FONT_COLOR);
        g.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_BIG));
        return g;
    }

}
