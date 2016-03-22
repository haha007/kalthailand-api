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
import th.co.krungthaiaxa.elife.api.model.DatedAmount;
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

@Service
public class SaleIllustrationService {
    private final static Logger logger = LoggerFactory.getLogger(SaleIllustrationService.class);

    private static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
    private static final String _fontNormal = "/saleillustration/PSL094.TTF";
    private static final String _fontBold = "/saleillustration/PSL096.TTF";
    private static final String IMG_SYMBOL_1 = "/saleillustration/symbol1.png";
    private static final String IMG_SYMBOL_2 = "/saleillustration/symbol2.png";
    private static final String IMG_SYMBOL_3 = "/saleillustration/symbol3.png";
    private static final String IMG_BENEFIT_1 = "/saleillustration/benefit1.png";
    private static final String IMG_BENEFIT_2 = "/saleillustration/benefit2.png";
    private static final String IMG_BENEFIT_3 = "/saleillustration/benefit3.png";
    private static final String IMG_BENEFIT_4 = "/saleillustration/benefit4.png";
    private static final String IMG_BENEFIT_5 = "/saleillustration/benefit5.png";
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
        table1.addCell(addData("ผลประโยชน์เงินจ่ายคืน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเลือกรับครั้งเดียว ณ ครบกำหนดสัญญา", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsMinimum().get(9).getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ระดับกลาง" + NEW_LINE + "(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsAverage().get(9).getValue()+quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksAverageBenefit().get(9).getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ระดับสูง" + NEW_LINE + "(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getEndOfContractBenefitsMaximum().get(9).getValue()+quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksMaximumBenefit().get(9).getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเลือกขอรับคืนทุกปี รวมตลอดสัญญา", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMinimumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ระดับกลาง" + NEW_LINE + "(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMediumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมรับผลประโยชน์ระดับสูง" + NEW_LINE + "(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMaximumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));
        table1.addCell(addLine(true, tbCols));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(graphContent, 70, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addLine(true, tbCols));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addLine(false, tbCols));

        tbCols = 3;
        PdfPTable table2 = new PdfPTable(tbCols);
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_SYMBOL_1), 50, null, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addLine(false, null));
        table2.addCell(addData("กรณีสะสมกับบริษัทด้วยอัตราดอกเบี้ยขั้นต่ำ 2% ต่อปี", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addImage(getResourceAsByteArray(IMG_SYMBOL_2), 50, null, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addLine(false, null));
        table2.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addImage(getResourceAsByteArray(IMG_SYMBOL_3), 50, null, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("เงินปันผลสูงสุด", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addLine(false, null));
        table2.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addLine(true, tbCols));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_1), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("ออมระยะสั้น", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addData("ระยะเวลาในการชำระเบี้ยประกันภัยหลักเพียง 6 ปี" + NEW_LINE + "คุ้มครองชีวิต 10 ปี ทำให้คุณสามารถวางแผน" + NEW_LINE + "ทางการเงินได้อย่างคล่องตัว", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_2), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("รับผลประโยชน์เพิ่มจากเงินปันผล", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addData("ตั้งแต่ปีที่ 7 จนถึงปีที่ 10 รวมสูงสุด 25.2% *", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_3), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("รับเงินจ่ายคืนประจำปีในอัตรา 2%", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addData("ของจำนวนเงินเอาประกันภัย นับตั้งแต่สิ้นปีที่ 1 จนถึง" + NEW_LINE + "ปีที่ 9 ครบสัญญารับเพิ่มอีก 182% รวมรับ" + NEW_LINE + "ผลประโยชน์เงินจ่ายคืนตามกรมธรรม์ขั้นต่ำ 200%", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_4), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("รับผลประโยชน์ตลอดอายุสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addData("ขั้นต่ำ 200% สูงสุดถึง 225.2% *", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_5), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table2.addCell(addData("รับสิทธิลดหย่อนภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addData("ผลประโยชน์เพิ่มเติมจากการลดหย่อนภาษี" + NEW_LINE + "สูงสุดที่ชำระ 35% ต่อปี", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table2.addCell(addLine(false, tbCols));
        table2.addCell(addLine(true, tbCols));
        table2.addCell(addData("สรุปผลประโยชน์สำหรับแบบประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData("ขั้นต่ำกรณีเสียชีวิตภายใน 3 ปีแรก", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table2.addCell(addData("ผลประโยชน์เพิ่มเติม", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData("ที่ชำระ " + quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription() + "% ต่อปี", getFontExtraSmallGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData(TAB + "ท่านจะได้รับผลประโยชน์จาก" + NEW_LINE + TAB + "การลดหย่อนภาษี ปีละ", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));
        table2.addCell(addData(TAB + "รวมผลประโยชน์ทางภาษีจาก" + NEW_LINE + TAB + "การชำระเบี้ยประกันภัย" + NEW_LINE + TAB + "ตลอดสัญญา เป็นเงิน", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getProduct10ECPremium().getYearlyTaxDeduction().getValue() * quote.getCommonData().getNbOfYearsOfPremium()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));
        table2.addCell(addLine(true, tbCols));
        table2.addCell(addData("* หมายเหตุ", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table2.addCell(addData("จำนวนเงินปันผลที่แสดงเป็นเพียงตัวเลขประมาณการเงินปันผลในอนาคตเท่านั้น บริษัทฯ ไม่สามารถรับประกันว่าท่านจะได้รับเงินปันผลตามประมาณการนี้ในอนาคต ท่านควรพิจารณาตัวเลขประมาณการเงินปันผลและสมมุติฐานโดยละเอียดโดยวิเคราะห์ถึงผลประกอบการในอนาคตและความคาดหวังในผลิตภัณฑ์นี้ด้วยตัวท่านเอง", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, content);
        document.open();
        document.add(table1);
        document.add(table2);
        document.close();
        content.close();
        return Pair.of(content.toByteArray(), "proposal_" + quote.getQuoteId() + "_" + getDate() + ".pdf");
    }

    private Double getMinimumRefundEveryYear(Quote quote) {
        return quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacks()
                .stream()
                .mapToDouble(DatedAmount::getValue)
                .sum();
    }

    private Double getMediumRefundEveryYear(Quote quote) {
        return quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksAverageDividende()
                .stream()
                .mapToDouble(DatedAmount::getValue)
                .sum();
    }

    private Double getMaximumRefundEveryYear(Quote quote) {
        return quote.getPremiumsData().getProduct10ECPremium().getYearlyCashBacksMaximumDividende()
                .stream()
                .mapToDouble(DatedAmount::getValue)
                .sum();
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
