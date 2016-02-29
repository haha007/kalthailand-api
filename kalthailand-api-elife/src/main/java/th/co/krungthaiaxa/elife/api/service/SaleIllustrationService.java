package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import th.co.krungthaiaxa.elife.api.model.DatedAmount;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SaleIllustrationService {
    private final static Logger logger = LoggerFactory.getLogger(SaleIllustrationService.class);

    private static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
    private static final String _fontNormal = "saleillustration/PSL094.TTF";
    private static final String _fontBold = "saleillustration/PSL096.TTF";
    private static final String IMG_SYMBOL_1 = "saleillustration/symbol1.png";
    private static final String IMG_SYMBOL_2 = "saleillustration/symbol2.png";
    private static final String IMG_SYMBOL_3 = "saleillustration/symbol3.png";
    private static final String IMG_BENEFIT_1 = "saleillustration/benefit1.png";
    private static final String IMG_BENEFIT_2 = "saleillustration/benefit2.png";
    private static final String IMG_BENEFIT_3 = "saleillustration/benefit3.png";
    private static final String IMG_BENEFIT_4 = "saleillustration/benefit4.png";
    private static final String IMG_BENEFIT_5 = "saleillustration/benefit5.png";
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

    public File generatePDF(Quote quote, String imgBase64) throws Exception {
        String PDF_OUT_PUT = this.getClass().getResource("saleillustration/temp/proposal.pdf").toString().replace("file:/", "");
        PDF_OUT_PUT = PDF_OUT_PUT.replace(".pdf", "_" + quote.getQuoteId() + "_" + getDate() + ".pdf");
        String IMG_GRAPH = convertBase64ToImage(imgBase64, quote.getQuoteId());

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "generatePDF"));
            logger.debug(String.format("quote is %1$s", quote.toString()));
            logger.debug(String.format("imgBase64 is %1$s", imgBase64));
        }

        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(PDF_OUT_PUT));
        document.open();

        Integer tbCols = 2;
        PdfPTable table = new PdfPTable(tbCols);

        table.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("งวดชำระ" + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        String MONEY_DECIMAL_FORMAT = "#,##0.00";
        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));

        table.addCell(addLine(true, tbCols));

        table.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("ผลประโยชน์เงินจ่ายคืน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("กรณีเลือกรับครั้งเดียว ณ ครบกำหนดสัญญา", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMinimum().stream().mapToDouble(DatedAmount::getValue).sum()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับกลาง" + NEW_LINE + "(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsAverage().stream().mapToDouble(DatedAmount::getValue).sum()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับสูง" + NEW_LINE + "(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMaximum().stream().mapToDouble(DatedAmount::getValue).sum()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("กรณีเลือกขอรับคืนทุกปี รวมตลอดสัญญา", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMaximumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับกลาง" + NEW_LINE + "(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMediumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(false, tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับสูง" + NEW_LINE + "(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMaximumRefundEveryYear(quote)) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(true, tbCols));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(IMG_GRAPH, 70, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addLine(true, tbCols));

        table.addCell(addLine(false, tbCols));

        table.addCell(addLine(false, tbCols));

        document.add(table);

        tbCols = 3;
        table = new PdfPTable(tbCols);

        table.addCell(addLine(false, tbCols));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_SYMBOL_1), 50, null, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, null));

        table.addCell(addData("กรณีสะสมกับบริษัทด้วยอัตราดอกเบี้ยขั้นต่ำ 2% ต่อปี", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addImage(getAbsoluteFilePath(IMG_SYMBOL_2), 50, null, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, null));

        table.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addImage(getAbsoluteFilePath(IMG_SYMBOL_3), 50, null, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินปันผลสูงสุด", getFontExtraSmallGrayStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(false, null));

        table.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addLine(true, tbCols));

        table.addCell(addLine(false, tbCols));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_BENEFIT_1), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("ออมระยะสั้น", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addData("ระยะเวลาในการชำระเบี้ยประกันภัยหลักเพียง 6 ปี" + NEW_LINE + "คุ้มครองชีวิต 10 ปี ทำให้คุณสามารถวางแผน" + NEW_LINE + "ทางการเงินได้อย่างคล่องตัว", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_BENEFIT_2), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับผลประโยชน์เพิ่มจากเงินปันผล", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addData("ตั้งแต่ปีที่ 7 จนถึงปีที่ 10 รวมสูงสุด 25.2%", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_BENEFIT_3), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับเงินจ่ายคืนประจำปีในอัตรา 2%", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addData("ของจำนวนเงินเอาประกันภัย นับตั้งแต่สิ้นปีที่ 1 จนถึง" + NEW_LINE + "ปีที่ 9 ครบสัญญารับเพิ่มอีก 182% รวมรับ" + NEW_LINE + "ผลประโยชน์เงินจ่ายคืนตามกรมธรรม์ขั้นต่ำ 200% *", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_BENEFIT_4), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับผลประโยชน์ตลอดอายุสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addData("ขั้นต่ำ 200% สูงสุดถึง 225.25%", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addImage(getAbsoluteFilePath(IMG_BENEFIT_5), SYMBOL_IMG_SIZE, tbCols, TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับสิทธิลดหย่อนภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addData("ผลประโยชน์เพิ่มเติมจากการลดหย่อนภาษี" + NEW_LINE + "สูงสุดที่ชำระ 35% ต่อปี", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_CENTER, null));

        table.addCell(addLine(false, tbCols));

        table.addCell(addLine(true, tbCols));

        table.addCell(addData("สรุปผลประโยชน์สำหรับแบบประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("ขั้นต่ำกรณีเสียชีวิตภายใน 3 ปีแรก", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));

        table.addCell(addData("ผลประโยชน์เพิ่มเติม", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("ที่ชำระ " + quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription() + "% ต่อปี", getFontExtraSmallGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData(TAB + "ท่านจะได้รับผลประโยชน์จาก" + NEW_LINE + TAB + "การลดหย่อนภาษี ปีละ", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));

        table.addCell(addData(TAB + "รวมผลประโยชน์ทางภาษีจาก" + NEW_LINE + TAB + "การชำระเบี้ยประกันภัย" + NEW_LINE + TAB + "ตลอดสัญญา เป็นเงิน", getFontNormalStyle(), tbCols - 1, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getYearlyTaxDeduction().getValue() * quote.getCommonData().getNbOfYearsOfPremium()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(true, tbCols));

        table.addCell(addData("* โปรดทราบ", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        table.addCell(addData("จำนวนเงินปันผลที่แสดงเป็นเพียงตัวเลขประมาณการเงินปันผลในอนาคตเท่านั้น บริษัทฯ ไม่สามารถรับประกันว่าท่านจะได้รับเงินปันผลตามประมาณการนี้ในอนาคต ท่านควรพิจารณาตัวเลขประมาณการเงินปันผลและสมมุติฐานโดยละเอียดโดยวิเคราะห์ถึงผลประกอบการในอนาคตและความคาดหวังในผลิตภัณฑ์นี้ด้วยตัวท่านเอง", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        document.add(table);

        document.close();

        return new File(PDF_OUT_PUT);

    }

    private Double getMinimumRefundEveryYear(Quote q) {
        Double sum = 0.0;
        for (Integer a = 0; a < q.getPremiumsData().getLifeInsurance().getYearlyCashBacks().size(); a++) {
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacks().get(a).getValue();
        }
        return sum;
    }

    private Double getMediumRefundEveryYear(Quote q) {
        Double sum = 0.0;
        for (Integer a = 0; a < q.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende().size(); a++) {
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende().get(a).getValue();
        }
        sum += getMinimumRefundEveryYear(q);
        return sum;
    }

    private Double getMaximumRefundEveryYear(Quote q) {
        Double sum = 0.0;
        for (Integer a = 0; a < q.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende().size(); a++) {
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende().get(a).getValue();
        }
        sum += getMinimumRefundEveryYear(q);
        return sum;
    }

    private String getDate() {
        Date d = new Date();
        return (new SimpleDateFormat("yyyyMMdd")).format(d);
    }

    private void houseKeeping() {
        File files = new File(getAbsoluteFilePath("saleillustration/temp"));
        if (files.listFiles() != null && files.listFiles().length != 0) {
            for (File f : files.listFiles()) {
                if (!f.getName().equals("proposal.pdf") && !f.getName().equals("graph.png")) {
                    String compareName = f.getName().substring(f.getName().length() - 12, f.getName().length() - 4);
                    if (!compareName.equals(getDate())) {
                        f.delete();
                    }
                }
            }
        }
    }

    private String convertBase64ToImage(String base64, String quoteId) throws Exception {
        String imgPath = getAbsoluteFilePath("saleillustration/temp/graph.png");
        imgPath = imgPath.replace(".png", "_" + quoteId + "_" + getDate() + ".png");

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "convertBase64ToImage"));
            logger.debug(String.format("base64 is %1$s", base64));
            logger.debug(String.format("quoteId is %1$s", quoteId));
        }

        houseKeeping();

        BASE64Decoder decoder = new BASE64Decoder();
        BufferedImage image;
        byte[] imageByte;
        imageByte = decoder.decodeBuffer(base64);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();
        File outputfile = new File(imgPath);
        ImageIO.write(image, "png", outputfile);
        return imgPath;
    }

    private static PdfPCell addImage(String imgPath, Integer imgScale, Integer colSpan, Integer horizontalAlignment) {
        Image img;
        try {
            img = Image.getInstance(imgPath);
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
            bfBold = BaseFont.createFont(getAbsoluteFilePath(_fontBold), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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
            bfNormal = BaseFont.createFont(getAbsoluteFilePath(_fontBold), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        return new Font(bfNormal, FONT_SIZE_NORMAL);
    }

    private Font getFontNormalGrayStyle() {
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(getAbsoluteFilePath(_fontBold), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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
            bfNormalBlue = BaseFont.createFont(getAbsoluteFilePath(_fontBold), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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
            bfExtraSmall = BaseFont.createFont(getAbsoluteFilePath(_fontNormal), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        return new Font(bfExtraSmall, FONT_SIZE_SMALL);
    }

    private Font getFontExtraSmallGrayStyle() {
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(getAbsoluteFilePath(_fontNormal), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.error("Unable to add image", e);
        }
        Font fontExtraSmallGray = new Font(bfExtraSmall, FONT_SIZE_SMALL);
        fontExtraSmallGray.setColor(new BaseColor(145, 145, 145));
        return fontExtraSmallGray;
    }

    private String getAbsoluteFilePath(String fileName) {
        return this.getClass().getResource(fileName).toString().replace("file:/", "");
    }

}
