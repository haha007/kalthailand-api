package th.co.krungthaiaxa.api.elife.products.iprotect;

import com.itextpdf.text.pdf.PdfPTable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.PdfIOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.product.ProductIProtectPremium;
import th.co.krungthaiaxa.api.elife.products.AbstractSaleIllustrationService;

import javax.inject.Inject;
import java.text.DecimalFormat;

/**
 * Created by SantiLik on 3/28/2016.
 */

@Service
public class IProtectSaleIllustrationService extends AbstractSaleIllustrationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(IProtectSaleIllustrationService.class);

    @Inject
    private MessageSource messageSource;

    @Override
    public Pair<byte[], String> generatePDF(Quote quote) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%1$s] .....", "generatePDF"));
            LOGGER.debug(String.format("quote is %1$s", quote.toString()));
        }

        ProductIProtectPremium p = quote.getPremiumsData().getProductIProtectPremium();
        Insured i = quote.getInsureds().get(0);
        Integer taxDeclared = (i.getDeclaredTaxPercentAtSubscription() == null ? 0 : i.getDeclaredTaxPercentAtSubscription());

        Integer tbCols = 2;
        PdfPTable pdfPTable = new PdfPTable(tbCols);
        pdfPTable.addCell(addData("เบี้ยประกัน", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(messageSource.getMessage("payment.mode." + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, thLocale), getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        String MONEY_DECIMAL_FORMAT = "#,##0.00";
        pdfPTable.addCell(addData((new DecimalFormat(MONEY_DECIMAL_FORMAT)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        if (quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(PeriodicityCode.EVERY_MONTH)) {
            pdfPTable.addCell(addData("ชำระเบี้ยรายเดือนผ่านทางบัญชี LINE Pay", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        }
        pdfPTable.addCell(addLine(true, tbCols));
        pdfPTable.addCell(addData("รายละเอียดของผลประโยชน์", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์กรณีเสียชีวิต", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("กรณีเสียชีวิต (ขั้นต่ำ)", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(p.getDeathBenefit().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์ครบกำหนดสัญญา", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("กรณีครบสัญญาเมื่ออายุ 85 ปี", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(p.getSumInsured().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ผลประโยชน์ทางภาษี", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("ลดหย่อนภาษีที่อัตราสูงสุด", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("ที่ชำระ " + String.valueOf(taxDeclared) + "% ต่อเดือน", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ท่านจะได้รับผลประโยชน์จากการลดหย่อนภาษี ปีละ", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(p.getYearlyTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("ท่านจะได้รับผลประโยชน์ทางภาษีจากการชำระเบี้ยประกันภัยตลอดสัญญา เป็นเงิน", getFontNormalStyle(), null, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData(toCurrency(p.getTotalTaxDeduction().getValue()) + " บาท", getFontNormalBlueStyle(), null, TB_HORIZONTAL_ALIGN_RIGHT, null));
        pdfPTable.addCell(addLine(false, tbCols));
        pdfPTable.addCell(addData("* หมายเหตุ", getFontNormalStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("- โปรดศึกษา อ่าน และทำความเข้าใจรายละเอียดเพิ่มเติมในเอกสารประกอบการขายของบริษัทฯ", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("- อายุขณะเอาประกันภัย 20-55 ปี", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        pdfPTable.addCell(addData("- เบี้ยประกันต่อเดือนต้องไม่ต่ำกว่า 1,000 บาท", getFontExtraSmallStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));

        return Pair.of(PdfIOUtil.writeToBytes(pdfPTable), "proposal_" + quote.getQuoteId() + "_" + getDate() + ".pdf");
    }

}
