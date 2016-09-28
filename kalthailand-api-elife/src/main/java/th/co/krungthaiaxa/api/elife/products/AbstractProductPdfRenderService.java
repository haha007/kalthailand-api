package th.co.krungthaiaxa.api.elife.products;

import static org.apache.commons.io.IOUtils.toByteArray;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import th.co.krungthaiaxa.api.elife.model.Quote;

@Component
public abstract class AbstractProductPdfRenderService implements InterfaceProductPdfRenderService {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractProductPdfRenderService.class);
	
	protected static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
	protected static final String _fontNormal = "/saleillustration/PSL094.TTF";
	protected static final String _fontBold = "/saleillustration/PSL096.TTF";
	protected static final Integer TB_HORIZONTAL_ALIGN_LEFT = 0;
	protected static final Integer TB_HORIZONTAL_ALIGN_CENTER = 1;
	protected static final Integer TB_HORIZONTAL_ALIGN_RIGHT = 2;
	protected static final Integer TB_VERTICAL_ALIGN_TOP = 0;
	protected static final Integer FONT_SIZE_HEADER = 27;
	protected static final Integer FONT_SIZE_NORMAL = 15;
	protected static final Integer FONT_SIZE_SMALL = 12;
	protected static final Integer BENEFIT_IMG_SIZE = 40;
	protected static final String TAB = "     ";
	protected static final String NEW_LINE = System.getProperty("line.separator");
	protected String MONEY_DECIMAL_FORMAT = "#,##0.00";
	protected final String PDF_NAME = "proposal_";
	protected final String PDF_EXTENSION = ".pdf";
	protected final String UNDERSCORE = "_";
	
    @Inject
    protected MessageSource messageSource;
    protected Locale thLocale = new Locale("th","");
    
    /*
     * custom value specific
     * */
    
    protected String toCurrency(Double d){
        return (new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(d);
    }
    
    /*
     * get pdf file name
     * */
    
    protected String getPDFName(Quote quote){
    	return PDF_NAME + quote.getQuoteId() + UNDERSCORE + getDate() + PDF_EXTENSION;
    }
    
    /*
     * pdf cell content specific
     * */

    protected static PdfPCell addImage(byte[] imgContent, Integer imgScale, Integer colSpan, Integer horizontalAlignment) {
        Image img;
        try {
            img = Image.getInstance(imgContent);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
            return new PdfPCell();
        }
        img.scalePercent(imgScale);
        PdfPCell cell = new PdfPCell(img);
        cell.setBorder(Rectangle.NO_BORDER);
        if (colSpan != null) {
            cell.setColspan(colSpan);
        }
        cell.setHorizontalAlignment(horizontalAlignment);
        return cell;
    }

    protected static PdfPCell addData(String msg, Font fontStyle, Integer colSpan, Integer horizontalAlignment, Integer verticalAlignment) {
        PdfPCell cell = new PdfPCell(new Phrase(new Paragraph(msg, fontStyle)));
        cell.setBorder(Rectangle.NO_BORDER);
        if (colSpan != null) {
            cell.setColspan(colSpan);
        }
        cell.setHorizontalAlignment(horizontalAlignment);
        if (verticalAlignment != null) {
            cell.setVerticalAlignment(verticalAlignment);
        }
        return cell;
    }

    protected static PdfPCell addLine(boolean border, Integer colSpan) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        if (border) {
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BORDER_COLOR);
        } else {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        if (colSpan != null) {
            cell.setColspan(colSpan);
        }
        return cell;
    }
    
    /*
     * Font style in pdf
     * */

    protected Font getFontHeaderStyle() {
        BaseFont bfBold = null;
        try {
            bfBold = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        Font fontHeader = new Font(bfBold, FONT_SIZE_HEADER);
        fontHeader.setColor(new BaseColor(14, 50, 131));
        return fontHeader;
    }

    protected Font getFontNormalStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        return new Font(bfNormal, FONT_SIZE_NORMAL);
    }

    protected Font getFontNormalGrayStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        Font fontNormalGray = new Font(bfNormal, FONT_SIZE_NORMAL);
        fontNormalGray.setColor(new BaseColor(145, 145, 145));
        return fontNormalGray;
    }

    protected Font getFontNormalDeepGrayStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        Font fontNormalGray = new Font(bfNormal, FONT_SIZE_NORMAL);
        fontNormalGray.setColor(new BaseColor(88, 88, 88));
        return fontNormalGray;
    }

    protected Font getFontNormalBlueStyle() {
        BaseFont bfNormalBlue = null;
        try {
            bfNormalBlue = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        Font fontNormalBlue = new Font(bfNormalBlue, FONT_SIZE_NORMAL);
        fontNormalBlue.setColor(new BaseColor(31, 139, 179));
        return fontNormalBlue;
    }

    protected Font getFontExtraSmallStyle() {
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        return new Font(bfExtraSmall, FONT_SIZE_SMALL);
    }

    protected Font getFontExtraSmallGrayStyle() {
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            LOGGER.error("Unable to add image", e);
        }
        Font fontExtraSmallGray = new Font(bfExtraSmall, FONT_SIZE_SMALL);
        fontExtraSmallGray.setColor(new BaseColor(145, 145, 145));
        return fontExtraSmallGray;
    }
    
    /*
     * get byte[] array source from path of file 
     * */

    protected byte[] getResourceAsByteArray(String imgPath){
        byte[] outPut = new byte[0];
        try {
            outPut = toByteArray(this.getClass().getResourceAsStream(imgPath));
        } catch (IOException e) {
            LOGGER.error("Unable to get resource as byte array", e);
        }
        return outPut;
    }    
    
    /*
     * get now date using within abstract class
     * */
    
    private String getDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

}
