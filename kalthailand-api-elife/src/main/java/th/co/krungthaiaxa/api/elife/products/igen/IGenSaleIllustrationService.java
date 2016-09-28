package th.co.krungthaiaxa.api.elife.products.igen;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.AbstractProductSaleIllustrationService;

@Service
public class IGenSaleIllustrationService extends AbstractProductSaleIllustrationService{

	@Override
	public Pair<byte[], String> generatePDF(Quote quote) throws DocumentException, IOException {
		
		if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%1$s] .....", "generatePDF for iGen"));
            LOGGER.debug(String.format("quote is %1$s", quote.toString()));
        }
		
		Integer tbCols = 2;
        PdfPTable table1 = new PdfPTable(tbCols);
        
        table1.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("งวดชำระDUMMY", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์เงินจ่ายคืนประจำปี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("และเงินครบกำหนดสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีรับครั้งเดียวโดยสะสมไว้กับ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ขั้นต่ำ " + toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("บริษัทฯ ด้วยอัตราดอกเบี้ยขั้นต่ำ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("2% ต่อปี รับรวม", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเลือกรับทุกปี รับรวม", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ปีที่ 1-3", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 4", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 5", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 6-10", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์การลดหย่อนภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ที่ชำระ DUMMY% ต่อปี", getFontExtraSmallGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ท่านจะได้รับผลประโยชน์จาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("การลดหย่อนภาษี ปีละ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมผลประโยชน์ทางภาษีจาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(0.0) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("การชำระเบี้ยประกันภัย", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ตลอดสัญญา เป็นเงิน", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("* โปรดศึกษารายละเอียดของความคุ้มครองและข้อยกเว้นต่างๆ เพิ่มเติมในเอกสารประกอบการขาย หรือกรมธรรม์ประกันภัยของบริษัทฯ", getFontNormalBlueStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("** สำหรับค่าเบี้ยประกันส่วนที่เป็นเบี้ยประกันชีวิต สามารถนำไปใช้ลดหย่อนภาษีเงินได้บุคคลธรรมดาตามอัตราที่กฏหมายกำหนด", getFontNormalBlueStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        
        
        
        
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, content);
        document.open();
        document.add(table1);
        document.close();
        content.close();
        return Pair.of(content.toByteArray(), getPDFName(quote));
	}

    

}
