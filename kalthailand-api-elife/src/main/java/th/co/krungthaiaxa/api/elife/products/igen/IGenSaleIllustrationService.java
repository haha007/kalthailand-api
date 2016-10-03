package th.co.krungthaiaxa.api.elife.products.igen;

import com.itextpdf.text.pdf.PdfPTable;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.PdfIOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.products.AbstractProductPdfRenderService;

@Service
public class IGenSaleIllustrationService extends AbstractProductPdfRenderService {

	/*
     * must be implement for sale illustration pdf file generated
	 * */

    @Override
    public Pair<byte[], String> generatePDF(Quote quote) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%1$s] .....", "generatePDF for iGen"));
            LOGGER.debug(String.format("quote is %1$s", quote.toString()));
        }

        Integer tbCols = 2;
        PdfPTable pdfPTable = new PdfPTable(tbCols);

        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        PremiumDetail premium = quote.getPremiumsData().getPremiumDetail();

        String productWording = "แบบประกัน" +
                getProductWordInProps(quote, true) +
                " (" + getProductWordInProps(quote, false) + ")";

        String dueWording = "งวดชำระ" + toThaiPaymentMode(quote.getPremiumsData().getFinancialScheduler().getPeriodicity());

        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription() == null ? 0 : insured.getDeclaredTaxPercentAtSubscription());

        pdfPTable.addCell(addData(productWording, getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(dueWording, getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์เงินจ่ายคืนประจำปี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("และเงินครบกำหนดสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("กรณีรับครั้งเดียวโดยสะสมไว้กับ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("ขั้นต่ำ " + toCurrency(premium.getYearlyCashBacksForEndOfContract().stream().reduce((first, second) -> second).get().getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("บริษัทฯ ด้วยอัตราดอกเบี้ยขั้นต่ำ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("2% ต่อปี รับรวม", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("กรณีเลือกรับทุกปี รับรวม", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyCashBacksForAnnual().stream().reduce((first, second) -> second).get().getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ปีที่ 1-3", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(0).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("ปีที่ 4", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(3).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("ปีที่ 5", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(4).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("ปีที่ 6-10", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyDeathBenefits().get(5).getAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์การลดหย่อนภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("ที่ชำระ " + taxDeclared + "% ต่อปี", getFontExtraSmallGrayStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ท่านจะได้รับผลประโยชน์จาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("การลดหย่อนภาษี ปีละ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("รวมผลประโยชน์ทางภาษีจาก", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(premium.getTotalTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addData("การชำระเบี้ยประกันภัย", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("ตลอดสัญญา เป็นเงิน", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("* โปรดศึกษารายละเอียดของความคุ้มครองและข้อยกเว้นต่างๆ เพิ่มเติมในเอกสารประกอบการขาย หรือกรมธรรม์ประกันภัยของบริษัทฯ", getFontNormalBlueStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("** สำหรับค่าเบี้ยประกันส่วนที่เป็นเบี้ยประกันชีวิต สามารถนำไปใช้ลดหย่อนภาษีเงินได้บุคคลธรรมดาตามอัตราที่กฏหมายกำหนด", getFontNormalBlueStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        return Pair.of(PdfIOUtil.writeToBytes(pdfPTable), getPDFName(quote));

    }

//	/*
//	 * must be implement for e receipt pdf file generated
//	 * */
//
//    @Override
//    public Pair<byte[], String> generateEreceiptPDF(Policy policy) throws DocumentException, IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//	/*
//	 * must be implement for da form pdf file generated
//	 * */
//
//    @Override
//    public Pair<byte[], String> generateDaFormPDF(Policy policy) throws DocumentException, IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//	/*
//	 * must be implement for application form not validate pdf file generate
//	 * */
//
//    @Override
//    public Pair<byte[], String> generateApplicationFormNotValidatedPDF(Policy policy)
//            throws DocumentException, IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//	/*
//	 * must be implement for application form validate pdf file generate
//	 * */
//
//    @Override
//    public Pair<byte[], String> generateApplicationFormValidatedPDF(Policy policy)
//            throws DocumentException, IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }

}
