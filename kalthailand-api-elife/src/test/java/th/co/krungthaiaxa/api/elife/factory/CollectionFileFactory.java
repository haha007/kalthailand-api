package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.service.RLSService;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class CollectionFileFactory {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    /**
     * The structure of Collection import file must be matched with structure inside {@link RLSService#importCollectionFile(InputStream)}
     *
     * @param policyNumbers
     * @return the inputstream of Excel file which contains policyNumbers need to be processed.
     */
    public static InputStream constructCollectionExcelFile(String... policyNumbers) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(RLSService.COLLECTION_FILE_SHEET_NAME);
        ExcelUtils.appendRow(
                sheet,
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_1),
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_2),
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_3),
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_4),
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_5),
                ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_6)
        );
        for (String policyNumber : policyNumbers) {
            ExcelUtils.appendRow(
                    sheet,
                    ExcelUtils.text("20160826"),
                    ExcelUtils.text("L"),
                    ExcelUtils.text("111"),
                    ExcelUtils.text(policyNumber),
                    ExcelUtils.text("M"),
                    ExcelUtils.integer(290.97)
            );
        }
        byte[] bytes = ExcelIOUtils.writeToBytes(workbook);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return bis;
    }
}
