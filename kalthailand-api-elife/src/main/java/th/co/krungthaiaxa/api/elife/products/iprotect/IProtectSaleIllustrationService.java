package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

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

import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import javax.inject.Inject;

import static org.apache.commons.io.IOUtils.toByteArray;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by SantiLik on 3/28/2016.
 */

@Service
public class IProtectSaleIllustrationService {

	private final static Logger logger = LoggerFactory.getLogger(IProtectSaleIllustrationService.class);
    private static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
    private static final String _fontNormal = "/saleillustration/PSL094.TTF";
    private static final String _fontBold = "/saleillustration/PSL096.TTF";
    private static final Integer TB_HORIZONTAL_ALIGN_LEFT = 0;
    private static final Integer TB_HORIZONTAL_ALIGN_CENTER = 1;
    private static final Integer TB_HORIZONTAL_ALIGN_RIGHT = 2;
    private static final Integer TB_VERTICAL_ALIGN_TOP = 0;
    private static final Integer FONT_SIZE_HEADER = 27;
    private static final Integer FONT_SIZE_NORMAL = 15;
    private static final Integer FONT_SIZE_SMALL = 12;
    private static final Integer BENEFIT_IMG_SIZE = 40;
    private static final String TAB = "     ";
    private static final String NEW_LINE = System.getProperty("line.separator");
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th","");
    private String MONEY_DECIMAL_FORMAT = "#,##0.00";
    
    public Pair<byte[], String> generatePDF(Quote quote) throws DocumentException, IOException {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "generatePDF"));
            logger.debug(String.format("quote is %1$s", quote.toString()));
        }

        ProductIProtectPremium p = quote.getPremiumsData().getProductIProtectPremium();
        Insured i = quote.getInsureds().get(0);
        Integer taxDeclared = (i.getDeclaredTaxPercentAtSubscription()==null?0:i.getDeclaredTaxPercentAtSubscription());

        
        Integer tbCols = 2;
        PdfPTable table1 = new PdfPTable(tbCols);
        table1.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(messageSource.getMessage("payment.mode."+quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale), getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        String MONEY_DECIMAL_FORMAT = "#,##0.00";
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        if(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(PeriodicityCode.EVERY_MONTH)){
            table1.addCell(addData("ชำระเบี้ยรายเดือนผ่านทางบัญชี LINE Pay", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        }
        table1.addCell(addLine(true, tbCols));
        table1.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเสียชีวิต (ขั้นต่ำ)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getDeathBenefit().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์ครบกำหนดสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("กรณีครบสัญญาเมื่ออายุ 85 ปี", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์ทางภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ที่ชำระ "+String.valueOf(taxDeclared)+"% ต่อเดือน", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ท่านจะได้รับผลประโยชน์จากการลดหย่อนภาษี ปีละ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ท่านจะได้รับผลประโยชน์ทางภาษีจากการชำระเบี้ยประกันภัยตลอดสัญญา เป็นเงิน", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getTotalTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("* หมายเหตุ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("- โปรดศึกษา อ่าน และทำความเข้าใจรายละเอียดเพิ่มเติมในเอกสารประกอบการขายของบริษัทฯ", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("- อายุขณะเอาประกันภัย 20-55 ปี", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("- เบี้ยประกันต่อเดือนต้องไม่ต่ำกว่า 1,000 บาท", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));


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

    private String toCurrency(Double d){
        return (new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(d);
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

    private Font getFontNormalDeepGrayStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontNormalGray = new Font(bfNormal, FONT_SIZE_NORMAL);
        fontNormalGray.setColor(new BaseColor(88, 88, 88));
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
            logger.error("Unable to get resource as byte array", e);
        }
        return outPut;
    }

}
