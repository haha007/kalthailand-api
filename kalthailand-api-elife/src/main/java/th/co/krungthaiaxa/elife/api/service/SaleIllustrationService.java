package th.co.krungthaiaxa.elife.api.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import th.co.krungthaiaxa.elife.api.model.DatedAmount;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.mail.util.ByteArrayDataSource;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by santilik on 2/24/2016.
 */

@Service
public class SaleIllustrationService {

    private final static Logger logger = LoggerFactory.getLogger(SaleIllustrationService.class);

    private static final BaseColor BORDER_COLOR = new BaseColor(218, 218, 218);
    private final String _fontNormal = (getClass().getClassLoader()).getResource("saleillustration/PSL094.TTF").toString().replace("file:/","");
    private final String _fontItalic = (getClass().getClassLoader()).getResource("saleillustration/PSL095.TTF").toString().replace("file:/","");
    private final String _fontBold = (getClass().getClassLoader()).getResource("saleillustration/PSL096.TTF").toString().replace("file:/","");
    private final String _fontBoldItalic = (getClass().getClassLoader()).getResource("saleillustration/PSL097.TTF").toString().replace("file:/","");
    private final String IMG_SYMBOL_1 = (getClass().getClassLoader()).getResource("saleillustration/symbol1.png").toString().replace("file:/","");
    private final String IMG_SYMBOL_2 = (getClass().getClassLoader()).getResource("saleillustration/symbol2.png").toString().replace("file:/","");
    private final String IMG_SYMBOL_3 = (getClass().getClassLoader()).getResource("saleillustration/symbol3.png").toString().replace("file:/","");
    private final String IMG_BENEFIT_1 = (getClass().getClassLoader()).getResource("saleillustration/benefit1.png").toString().replace("file:/","");
    private final String IMG_BENEFIT_2 = (getClass().getClassLoader()).getResource("saleillustration/benefit2.png").toString().replace("file:/","");
    private final String IMG_BENEFIT_3 = (getClass().getClassLoader()).getResource("saleillustration/benefit3.png").toString().replace("file:/","");
    private final String IMG_BENEFIT_4 = (getClass().getClassLoader()).getResource("saleillustration/benefit4.png").toString().replace("file:/","");
    private final String IMG_BENEFIT_5 = (getClass().getClassLoader()).getResource("saleillustration/benefit5.png").toString().replace("file:/","");
    private static final Integer TB_HORIZONTAL_ALIGN_LEFT = 0;
    private static final Integer TB_HORIZONTAL_ALIGN_CENTER = 1;
    private static final Integer TB_HORIZONTAL_ALIGN_RIGHT = 2;
    private static final Integer TB_VERTICAL_ALIGN_TOP = 0;
    private static final Integer TB_VERTICAL_ALIGN_MIDDLE = 1;
    private static final Integer TB_VERTICAL_ALIGN_BOTTOM = 2;
    private static final Integer FONT_SIZE_HEADER = 27;
    private static final Integer FONT_SIZE_NORMAL = 15;
    private static final Integer FONT_SIZE_SMALL = 12;
    private static final Integer SYMBOL_IMG_SIZE = 35;
    private static final String TAB = "     ";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static PdfPTable table;
    private static PdfPCell cell;
    private static Image img;
    private static Integer tbCols;
    private static Document document;
    private static PdfWriter writer;
    private static String MONEY_DECIMAL_FORMAT = "#,##0.00";

    public File generatePDF(Quote quote, String imgBase64)throws Exception{

        String PDF_OUT_PUT = (getClass().getClassLoader()).getResource("saleillustration/temp/proposal.pdf").toString().replace("file:/","");
        PDF_OUT_PUT = PDF_OUT_PUT.replace(".pdf","_"+quote.getQuoteId()+"_"+getDate()+".pdf");
        String IMG_GRAPH = convertBase64ToImage(imgBase64, quote.getQuoteId());

        if(logger.isDebugEnabled()){
            logger.debug(String.format("[%1$s] .....","generatePDF"));
            logger.debug(String.format("quote is %1$s",quote.toString()));
            logger.debug(String.format("imgBase64 is %1$s",imgBase64));
        }

        document = new Document(PageSize.A4, 20, 20, 20, 20);
        writer = PdfWriter.getInstance(document, new FileOutputStream(PDF_OUT_PUT));
        document.open();

        tbCols = 2;
        table = new PdfPTable(tbCols);

        table.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("งวดชำระ"+getPeriodicityThai(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString()), getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,null));

        table.addCell(addLine(true,tbCols));

        table.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("ผลประโยชน์เงินจ่ายคืน", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("กรณีเลือกรับครั้งเดียว ณ ครบกำหนดสัญญา", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMinimum().stream().mapToDouble(DatedAmount::getValue).sum())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับกลาง"+NEW_LINE+"(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsAverage().stream().mapToDouble(DatedAmount::getValue).sum())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับสูง"+NEW_LINE+"(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getEndOfContractBenefitsMaximum().stream().mapToDouble(DatedAmount::getValue).sum())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("กรณีเลือกขอรับคืนทุกปี รวมตลอดสัญญา", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ขั้นต่ำ", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMaximumRefundEveryYear(quote))+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับกลาง"+NEW_LINE+"(รวมเงินปันผล*ระดับกลาง)", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMediumRefundEveryYear(quote))+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(false,tbCols));

        table.addCell(addData("รวมรับผลประโยชน์ระดับสูง"+NEW_LINE+"(รวมเงินปันผล*ระดับสูง)", getFontNormalStyle(),null,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(getMaximumRefundEveryYear(quote))+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(true,tbCols));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_GRAPH,70, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addLine(true,tbCols));

        table.addCell(addLine(false,tbCols));

        table.addCell(addLine(false,tbCols));

        document.add(table);

        tbCols = 3;
        table = new PdfPTable(tbCols);

        table.addCell(addLine(false,tbCols));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_SYMBOL_1,50, null,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,null));

        table.addCell(addData("กรณีสะสมกับบริษัทด้วยอัตราดอกเบี้ยขั้นต่ำ 2% ต่อปี", getFontExtraSmallStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addImage(IMG_SYMBOL_2,50, null,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)", getFontExtraSmallGrayStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,null));

        table.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addImage(IMG_SYMBOL_3,50, null,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("เงินปันผลสูงสุด", getFontExtraSmallGrayStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(false,null));

        table.addCell(addData("กรณีรับเงินสด", getFontExtraSmallStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addLine(true,tbCols));

        table.addCell(addLine(false,tbCols));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_BENEFIT_1,SYMBOL_IMG_SIZE, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("ออมระยะสั้น", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addData("ระยะเวลาในการชำระเบี้ยประกันภัยหลักเพียง 6 ปี"+NEW_LINE+"คุ้มครองชีวิต 10 ปี ทำให้คุณสามารถวางแผน"+NEW_LINE+"ทางการเงินได้อย่างคล่องตัว", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_BENEFIT_2,SYMBOL_IMG_SIZE, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับผลประโยชน์เพิ่มจากเงินปันผล", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addData("ตั้งแต่ปีที่ 7 จนถึงปีที่ 10 รวมสูงสุด 25.2%", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_BENEFIT_3,SYMBOL_IMG_SIZE, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับเงินจ่ายคืนประจำปีในอัตรา 2%", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addData("ของจำนวนเงินเอาประกันภัย นับตั้งแต่สิ้นปีที่ 1 จนถึง"+NEW_LINE+"ปีที่ 9 ครบสัญญารับเพิ่มอีก 182% รวมรับ"+NEW_LINE+"ผลประโยชน์เงินจ่ายคืนตามกรมธรรม์ขั้นต่ำ 200% *", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_BENEFIT_4,SYMBOL_IMG_SIZE, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับผลประโยชน์ตลอดอายุสัญญา", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addData("ขั้นต่ำ 200% สูงสุดถึง 225.25%", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addImage(IMG_BENEFIT_5,SYMBOL_IMG_SIZE, tbCols,TB_HORIZONTAL_ALIGN_CENTER));

        table.addCell(addData("รับสิทธิลดหย่อนภาษี", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addData("ผลประโยชน์เพิ่มเติมจากการลดหย่อนภาษี"+NEW_LINE+"สูงสุดที่ชำระ 35% ต่อปี", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_CENTER,null));

        table.addCell(addLine(false,tbCols));

        table.addCell(addLine(true,tbCols));

        table.addCell(addData("สรุปผลประโยชน์สำหรับแบบประกัน", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("ขั้นต่ำกรณีเสียชีวิตภายใน 3 ปีแรก", getFontNormalStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getSumInsured().getValue())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,null));

        table.addCell(addData("ผลประโยชน์เพิ่มเติม", getFontHeaderStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("ที่ชำระ "+quote.getInsureds().get(0).getDeclaredTaxPercentAtSubscription()+"% ต่อปี", getFontExtraSmallGrayStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData(TAB+"ท่านจะได้รับผลประโยชน์จาก"+NEW_LINE+TAB+"การลดหย่อนภาษี ปีละ", getFontNormalStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getYearlyTaxDeduction().getValue())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,TB_VERTICAL_ALIGN_TOP));

        table.addCell(addData(TAB+"รวมผลประโยชน์ทางภาษีจาก"+NEW_LINE+TAB+"การชำระเบี้ยประกันภัย"+NEW_LINE+TAB+"ตลอดสัญญา เป็นเงิน", getFontNormalStyle(),tbCols-1,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getLifeInsurance().getYearlyTaxDeduction().getValue()*quote.getCommonData().getNbOfYearsOfPremium())+" บาท", getFontNormalBlueStyle(),null,TB_HORIZONTAL_ALIGN_RIGHT,TB_VERTICAL_ALIGN_TOP));

        table.addCell(addLine(true,tbCols));

        table.addCell(addData("* โปรดทราบ", getFontNormalGrayStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        table.addCell(addData("จำนวนเงินปันผลที่แสดงเป็นเพียงตัวเลขประมาณการเงินปันผลในอนาคตเท่านั้น บริษัทฯ ไม่สามารถรับประกันว่าท่านจะได้รับเงินปันผลตามประมาณการนี้ในอนาคต ท่านควรพิจารณาตัวเลขประมาณการเงินปันผลและสมมุติฐานโดยละเอียดโดยวิเคราะห์ถึงผลประกอบการในอนาคตและความคาดหวังในผลิตภัณฑ์นี้ด้วยตัวท่านเอง", getFontNormalGrayStyle(),tbCols,TB_HORIZONTAL_ALIGN_LEFT,null));

        document.add(table);

        document.close();

        return new File(PDF_OUT_PUT);

    }

    private String getPeriodicityThai(String periodicityEng){
        String val = "";
        if(periodicityEng.equals("EVERY_YEAR")){
            val = "รายปี";
        }else if(periodicityEng.equals("EVERY_HALF_YEAR")){
            val = "ราย 6 เดือน";
        }else if(periodicityEng.equals("EVERY_QUARTER")){
            val = "ราย 3 เดือน";
        }else if(periodicityEng.equals("EVERY_MONTH")){
            val = "รายเดือน";
        }
        return val;
    }

    private Double getMinimumRefundEveryYear(Quote q){
        Double sum = 0.0;
        for(Integer a=0;a<q.getPremiumsData().getLifeInsurance().getYearlyCashBacks().size();a++){
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacks().get(a).getValue();
        }
        return sum;
    }

    private Double getMediumRefundEveryYear(Quote q){
        Double sum = 0.0;
        for(Integer a=0;a<q.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende().size();a++){
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacksAverageDividende().get(a).getValue();
        }
        sum += getMinimumRefundEveryYear(q);
        return sum;
    }

    private Double getMaximumRefundEveryYear(Quote q){
        Double sum = 0.0;
        for(Integer a=0;a<q.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende().size();a++){
            sum += q.getPremiumsData().getLifeInsurance().getYearlyCashBacksMaximumDividende().get(a).getValue();
        }
        sum += getMinimumRefundEveryYear(q);
        return sum;
    }

    private String getDate(){
        Date d = new Date();
        return (new SimpleDateFormat("yyyyMMdd")).format(d);
    }

    private void houseKeeping(){
        File files = new File((getClass().getClassLoader()).getResource("saleillustration/temp").toString().replace("file:/",""));
        for(File f : files.listFiles()){
            if(!f.getName().equals("proposal.pdf")&&!f.getName().equals("graph.png")){
                String compareName = f.getName().substring(f.getName().length()-12,f.getName().length()-4);
                if(!compareName.equals(getDate())){
                    f.delete();
                }
            }
        }
    }

    private String convertBase64ToImage(String base64, String quoteId)throws Exception{

        String imgPath = (getClass().getClassLoader()).getResource("saleillustration/temp/graph.png").toString().replace("file:/","");
        imgPath = imgPath.replace(".png","_"+quoteId+"_"+getDate()+".png");

        if(logger.isDebugEnabled()){
            logger.debug(String.format("[%1$s] .....","convertBase64ToImage"));
            logger.debug(String.format("base64 is %1$s",base64));
            logger.debug(String.format("quoteId is %1$s",quoteId));
        }

        houseKeeping();

        BASE64Decoder decoder = new BASE64Decoder();
        BufferedImage image = null;
        byte[] imageByte;
        imageByte = decoder.decodeBuffer(base64);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();
        File outputfile = new File(imgPath);
        ImageIO.write(image, "png", outputfile);
        return imgPath;
    }

    private static PdfPCell addImage(String imgPath, Integer imgScale, Integer colSpan, Integer horizontalAlignment){
        try {
            img = Image.getInstance(imgPath);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        img.scalePercent(imgScale);
        cell = new PdfPCell(img);
        cell.setBorder(Rectangle.NO_BORDER);
        if(colSpan!=null){
            cell.setColspan(colSpan);
        }
        cell.setHorizontalAlignment(horizontalAlignment);
        return cell;
    }

    private static PdfPCell addData(String msg, Font fontStyle, Integer colSpan, Integer horizontalAlignment, Integer verticalAlignment){
        cell = new PdfPCell(new Phrase(new Paragraph(msg, fontStyle)));
        cell.setBorder(Rectangle.NO_BORDER);
        if(colSpan!=null){
            cell.setColspan(colSpan);
        }
        cell.setHorizontalAlignment(horizontalAlignment);
        if(verticalAlignment!=null){
            cell.setVerticalAlignment(verticalAlignment);
        }
        return cell;
    }

    private static PdfPCell addLine(boolean border, Integer colSpan){
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        if(border==true){
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BORDER_COLOR);
        }else{
            cell.setBorder(Rectangle.NO_BORDER);
        }
        if(colSpan!=null){
            cell.setColspan(colSpan);
        }
        return cell;
    }

    private Font getFontHeaderStyle(){
        BaseFont bfBold = null;
        try {
            bfBold = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontHeader = new Font(bfBold, FONT_SIZE_HEADER);
        fontHeader.setColor(new BaseColor(14, 50, 131));
        return fontHeader;
    }

    private Font getFontNormalStyle(){
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontNormal = new Font(bfNormal, FONT_SIZE_NORMAL);
        return fontNormal;
    }

    private Font getFontNormalGrayStyle(){
        BaseFont bfNormal = null;
        try {
            bfNormal = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontNormalGray = new Font(bfNormal, FONT_SIZE_NORMAL);
        fontNormalGray.setColor(new BaseColor(145, 145, 145));
        return fontNormalGray;
    }

    private Font getFontNormalBlueStyle(){
        BaseFont bfNormalBlue = null;
        try {
            bfNormalBlue = BaseFont.createFont(_fontBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontNormalBlue = new Font(bfNormalBlue, FONT_SIZE_NORMAL);
        fontNormalBlue.setColor(new BaseColor(31, 139, 179));
        return fontNormalBlue;
    }

    private Font getFontExtraSmallStyle(){
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontExtraSmall = new Font(bfExtraSmall, FONT_SIZE_SMALL);
        return fontExtraSmall;
    }

    private Font getFontExtraSmallGrayStyle(){
        BaseFont bfExtraSmall = null;
        try {
            bfExtraSmall = BaseFont.createFont(_fontNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font fontExtraSmallGray = new Font(bfExtraSmall, FONT_SIZE_SMALL);
        fontExtraSmallGray.setColor(new BaseColor(145, 145, 145));
        return fontExtraSmallGray;
    }


}
