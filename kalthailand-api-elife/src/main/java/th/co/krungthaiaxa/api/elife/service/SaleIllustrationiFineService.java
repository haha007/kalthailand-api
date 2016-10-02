package th.co.krungthaiaxa.api.elife.service;

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
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.ProductIFinePremium;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final String IMG_BENEFIT_1 = "/saleillustration/ifine/iFine_img1.png";
    private static final String IMG_BENEFIT_2 = "/saleillustration/ifine/iFine_img2.png";
    private static final String IMG_BENEFIT_3 = "/saleillustration/ifine/iFine_img3.png";
    private static final String IMG_BENEFIT_4 = "/saleillustration/ifine/iFine_img4.png";
    private static final String IMG_BENEFIT_5 = "/saleillustration/ifine/iFine_img5.png";
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

        ProductIFinePremium p = quote.getPremiumsData().getProductIFinePremium();

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
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_1), BENEFIT_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addData("คุ้มครองการเสียชีวิตทุกกรณี *", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("สูงสุด " + toCurrency(p.getSumInsured().getValue()) + " บาท", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("ด้วยการพิจารณารับประกันภัยแบบยืดหยุ่น", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("ตอบคำถามสุขภาพเพียง 3 ข้อ", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_2), BENEFIT_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addData("คุ้มครองกรณีเสียชีวิตจากอุบัติเหตุ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("เพิ่มเติมจากผลประโยชน์กรณีเสียชีวิต *", getFontNormalDeepGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("และรับสิทธิประโยชน์ 2 เท่า จากอุบัติเหตุที่ระบุตามเงื่อนไข *", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData(toCurrency(p.getDeathByAccidentInPublicTransport().getValue()) + " บาท", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_3), BENEFIT_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addData("คุ้มครองกรณี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("สูญเสียอวัยวะจากอุบัติเหตุ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("สูงสุด 100% ของจำนวนเงินเอาประกันภัยต่อปีกรมธรรม์", getFontNormalDeepGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("ตามที่ระบุตารางรายละเอียดผลประโยชน์", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_4), BENEFIT_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addData("ค่ารักษาพยาบาล", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("เนื่องจากอุบัติเหตุต่อครั้ง", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("ตามที่เกิดขึ้นจริง โดยไม่เกินจำนวนที่กำหนดไว้", getFontNormalDeepGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData(toCurrency(p.getMedicalCareCost().getValue()) + " บาท", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addImage(getResourceAsByteArray(IMG_BENEFIT_5), BENEFIT_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));
        table1.addCell(addData("ผลประโยชน์ชดเชยรายวัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("เนื่องจากอุบัติเหตุ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("กรณีรักษาตัวเป็นผู้ป่วยใน", getFontNormalDeepGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData(toCurrency(p.getHospitalizationSumInsured().getValue()) + " บาท", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addData("สูงสุดไม่เกิน 365 วัน ต่อปีกรมธรรม์", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("จากทุกสาเหตุ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("เนื่องจากอุบัติเหตุ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getAccidentSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("(เพิ่มเติมจากทุกสาเหตุ)", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("เนื่องจากอุบัติเหตุสาธารณะ *", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getDeathByAccidentInPublicTransport().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("(เพิ่มเติมจากทุกสาเหตุ)", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ความคุ้มครองกรณีสูยเสียอวัยวะ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("จากอุบัติเหตุ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียมือหรือเท้า 1 ข้างหรือมากกว่า", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfHandOrLeg().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียสายตา 1 ข้างหรือมากกว่า", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfSight().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียการได้ยิน", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfHearingMin().getValue()) + " - " + toCurrency(p.getLossOfHearingMax().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียการพูด", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfSpeech().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียกระจกตาทั้งสองข้าง", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfCorneaForBothEyes().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("สูญเสียนิ้วมือหรือนิ้วเท้า", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLossOfFingersMin().getValue()) + " - " + toCurrency(p.getLossOfFingersMax().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("(ขึ้นอยู่กับจำนวนข้อที่สูญเสีย)", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กระดูกขาหรือสะบ้า แตกหักและรักษาไม่หาย", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getNoneCurableBoneFracture().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ขาหดสั้นลงอย่างน้อย 5 เซนติเมตร", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getLegsShortenBy5cm().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("บาดแผลไฟไหม้ฉกรรจ์", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getBurnInjuryMin().getValue()) + " - " + toCurrency(p.getBurnInjuryMax().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("(เสียหายตั้งแต่ 2% ขึ้นไปของผิวหนังทั้งหมด)", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ค่ารักษาพยาบาลเนื่องจากอุบัติเหตุ", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ตามที่เกิดขึ้นจริง แต่ไม่เกินครั้งละ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getMedicalCareCost().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์ชดเชยรายวัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเข้ารักษาตัวในโรงพยาบาล", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(p.getHospitalizationSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("(จำนวนวันสูงสุดไม่เกิน 365 วัน)", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(true, tbCols));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("* หมายเหตุ:", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ข้อยกเว้นที่สำคัญ:", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("1) การฆ่าตัวตาย หรือทำร้ายตัวเอง", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("2) ขณะที่มีการล่าสัตว์ แข่งรถ แข่งเรือ แข่งม้า เล่นหรือแข่งสกีทุกชนิด เล่น/แข่งสเก็ต ชกมวย โดดร่ม ขึ้น/ลง/โดยสารในบอลลูน เครื่องร่อน บันจี้จั๊มพ์ ปีนหรือไต่เขาที่ต้องใช้เครื่องมือช่วย ดำน้ำที่ต้องใช้ถังอากาศและเครื่องช่วยหายใจใต้น้ำ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("3) ขึ้นหรือลงหรือโดยสาร อยู่ในอากาศยานที่มิได้จดทะเบียนเพื่อบรรทุกผู้โดยสาร", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("คุ้มครอง 2 เท่า กรณีเสียชีวิตจากอุบัติเหตุ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("1) อุบัติเหตุที่เกิดขึ้นจากยานพาหนะสาธารณะทางบก", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("2) ขณะที่อยู่ในลิฟท์", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("3) การเสียชีวิตที่เกิดขึ้น เนื่องจากไฟไหม้โรงมหรศพ โรงแรม หรืออาคารสาธารณะ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("อุบัติเหตุสาธารณะ ได้แก่", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("1) ผู้เอาประกันภัยโดยสารในฐานะผู้โดยสารในยานพาหนะสาธารณะที่ขับเคลื่อนด้วยเครื่องจักรกล ซึ่งผู้ทำการขนส่งสาธารณะเป็นผู้รับจ้างทำการขนส่งบนเส้นทางขนส่งทางบกที่ได้กำหนดไว้ หรือ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("2) ขณะที่ผู้เอาประกันภัยอยู่ในลิฟท์ (ยกเว้นลิฟท์ที่ใช้ในเหมืองแร่หรือสถานที่ก่อสร้าง) หรือ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("3) เกิดขึ้นเนื่องจากไฟไหม้โรงมหรสพ โรงแรม หรืออาคารสาธารณะอื่นใด ซึ่งผู้เอาประกันภัยอยู่ ณ สถานที่นั้น ในขณะที่เริ่มไฟไหม้", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData(TAB + "- โปรดศึกษา อ่าน และทำความเข้าใจรายละเอียดเพิ่มเติมในเอกสารประกอบการขายของบริษัทฯ", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(TAB + "- อายุขณะขอเอาประกันภัย 20 - 59 ปี", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(TAB + "- จำนวนเงินเอาประกันภัยขั้นต่ำ 100,000 บาท", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));


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
