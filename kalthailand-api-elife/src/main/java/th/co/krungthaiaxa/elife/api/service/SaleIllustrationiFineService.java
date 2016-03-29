package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * Created by SantiLik on 3/28/2016.
 */

@Service
public class SaleIllustrationiFineService {

    private final static Logger logger = LoggerFactory.getLogger(SaleIllustrationiFineService.class);
    private static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
    private static final String _fontNormal = "/saleillustration/PSL094.TTF";
    private static final String _fontBold = "/saleillustration/PSL096.TTF";
    /*private static final String IMG_SYMBOL_1 = "/saleillustration/10ec/symbol1.png";
    private static final String IMG_SYMBOL_2 = "/saleillustration/10ec/symbol2.png";
    private static final String IMG_SYMBOL_3 = "/saleillustration/10ec/symbol3.png";
    private static final String IMG_BENEFIT_1 = "/saleillustration/10ec/benefit1.png";
    private static final String IMG_BENEFIT_2 = "/saleillustration/10ec/benefit2.png";
    private static final String IMG_BENEFIT_3 = "/saleillustration/10ec/benefit3.png";
    private static final String IMG_BENEFIT_4 = "/saleillustration/10ec/benefit4.png";
    private static final String IMG_BENEFIT_5 = "/saleillustration/10ec/benefit5.png";*/
    private static final Integer TB_HORIZONTAL_ALIGN_LEFT = 0;
    private static final Integer TB_HORIZONTAL_ALIGN_CENTER = 1;
    private static final Integer TB_HORIZONTAL_ALIGN_RIGHT = 2;
    private static final Integer TB_VERTICAL_ALIGN_TOP = 0;
    private static final Integer FONT_SIZE_HEADER = 27;
    private static final Integer FONT_SIZE_NORMAL = 15;
    private static final Integer FONT_SIZE_SMALL = 12;
    private static final Integer SYMBOL_IMG_SIZE = 35;
    private static final String TAB = "     ";
    private static final String NEW_LINE = System.getProperty("line.separator");
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th","");

    public Pair<byte[], String> generatePDF(Quote quote, String imgBase64) throws DocumentException, IOException {
        byte[] graphContent = Base64.getDecoder().decode(imgBase64.getBytes());

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "generatePDF"));
            logger.debug(String.format("quote is %1$s", quote.toString()));
            logger.debug(String.format("imgBase64 is %1$s", imgBase64));
        }

        Integer tbCols = 2;
        PdfPTable table1 = new PdfPTable(tbCols);
        table1.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("งวดชำระ" + messageSource.getMessage("payment.mode."+quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale), getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        String MONEY_DECIMAL_FORMAT = "#,##0.00";
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        if(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(PeriodicityCode.EVERY_MONTH)){
            table1.addCell(addData("ชำระเบี้ยรายเดือนผ่านทางบัญชี LINE Pay", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        }
        table1.addCell(addLine(true, tbCols));
        table1.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, content);
        document.open();
        document.add(table1);
        //document.add(table2);
        document.close();
        content.close();
        return Pair.of(content.toByteArray(), "proposal_" + quote.getQuoteId() + "_" + getDate() + ".pdf");

    }

    private String getDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private static PdfPCell addImage(byte[] imgContent, Integer imgScale, Integer colSpan, Integer horizontalAlignment) {
        Image img;
        try {
            img = Image.getInstance(imgContent);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
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

    private static PdfPCell addData(String msg, Font fontStyle, Integer colSpan, Integer horizontalAlignment, Integer verticalAlignment) {
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

    private static PdfPCell addLine(boolean border, Integer colSpan) {
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

    private Font getFontHeaderStyle() {
        BaseFont bfBold = null;
        try {
            bfBold = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontHeader = new Font(bfBold, FONT_SIZE_HEADER);
        fontHeader.setColor(new BaseColor(14, 50, 131));
        return fontHeader;
    }

    private Font getFontNormalStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        return new Font(bfNormal, FONT_SIZE_NORMAL);
    }

    private Font getFontNormalGrayStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontNormalGray = new Font(bfNormal, FONT_SIZE_NORMAL);
        fontNormalGray.setColor(new BaseColor(145, 145, 145));
        return fontNormalGray;
    }

    private Font getFontNormalBlueStyle() {
        BaseFont bfNormalBlue = null;
        try {
            bfNormalBlue = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontNormalBlue = new Font(bfNormalBlue, FONT_SIZE_NORMAL);
        fontNormalBlue.setColor(new BaseColor(31, 139, 179));
        return fontNormalBlue;
    }

    private Font getFontExtraSmallStyle() {
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        return new Font(bfExtraSmall, FONT_SIZE_SMALL);
    }

    private Font getFontExtraSmallGrayStyle() {
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontExtraSmallGray = new Font(bfExtraSmall, FONT_SIZE_SMALL);
        fontExtraSmallGray.setColor(new BaseColor(145, 145, 145));
        return fontExtraSmallGray;
    }

    private byte[] getResourceAsByteArray(String imgPath){
        byte[] outPut = new byte[0];
        try {
            outPut = toByteArray(this.getClass().getResourceAsStream(imgPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outPut;
    }
}
