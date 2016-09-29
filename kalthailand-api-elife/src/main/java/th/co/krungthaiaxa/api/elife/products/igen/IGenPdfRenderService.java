package th.co.krungthaiaxa.api.elife.products.igen;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.CycleDetectingLockFactory.Policy;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.AbstractProductPdfRenderService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class IGenPdfRenderService extends AbstractProductPdfRenderService {
	
	public static void main(String args[]){
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("o");
		System.out.println(list.stream().reduce((first, second) -> second).get());
	}

	
	/*
	 * must be implement for sale illustration pdf file generated
	 * */

	@Override
	public Pair<byte[], String> generateSaleIllustrationPDF(Quote quote) throws DocumentException, IOException {
		
		if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%1$s] .....", "generatePDF for iGen"));
            LOGGER.debug(String.format("quote is %1$s", quote.toString()));
        }
		
		Integer tbCols = 2;
        PdfPTable table1 = new PdfPTable(tbCols);
        
        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        ProductIGenPremium premium = quote.getPremiumsData().getProductIGenPremium();
        
        String productWording = "แบบประกัน" + 
        messageSource.getMessage("product.id."+quote.getCommonData().getProductId(), null, thLocale) +
        " (" + messageSource.getMessage("product.id."+quote.getCommonData().getProductId(), null, null) +")";
        
        String dueWording = "งวดชำระ" +
        messageSource.getMessage("payment.mode."+quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale);
        
        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription()==null?0:insured.getDeclaredTaxPercentAtSubscription());
        
        table1.addCell(addData(productWording, getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(dueWording, getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์เงินจ่ายคืนประจำปี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("และเงินครบกำหนดสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีรับครั้งเดียวโดยสะสมไว้กับ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ขั้นต่ำ " + toCurrency(premium.getYearlyCashBacksForEndOfContract().stream().reduce((first, second) -> second).get().getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("บริษัทฯ ด้วยอัตราดอกเบี้ยขั้นต่ำ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("2% ต่อปี รับรวม", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("กรณีเลือกรับทุกปี รับรวม", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyCashBacksForAnnual().stream().reduce((first, second) -> second).get().getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ปีที่ 1-3", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(0).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 4", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(3).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 5", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(4).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("ปีที่ 6-10", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(5).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ผลประโยชน์การลดหย่อนภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData("ที่ชำระ "+taxDeclared+"% ต่อปี", getFontExtraSmallGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("ท่านจะได้รับผลประโยชน์จาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        table1.addCell(addData("การลดหย่อนภาษี ปีละ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addLine(false, tbCols));
        table1.addCell(addData("รวมผลประโยชน์ทางภาษีจาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        table1.addCell(addData(toCurrency(premium.getTotalTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
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
	
	/*
	 * must be implement for e receipt pdf file generated
	 * */

	@Override
	public Pair<byte[], String> generateEreceiptPDF(Policy policy) throws DocumentException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * must be implement for da form pdf file generated
	 * */

	@Override
	public Pair<byte[], String> generateDaFormPDF(Policy policy) throws DocumentException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * must be implement for application form not validate pdf file generate
	 * */

	@Override
	public Pair<byte[], String> generateApplicationFormNotValidatedPDF(Policy policy)
			throws DocumentException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * must be implement for application form validate pdf file generate
	 * */

	@Override
	public Pair<byte[], String> generateApplicationFormValidatedPDF(Policy policy)
			throws DocumentException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
