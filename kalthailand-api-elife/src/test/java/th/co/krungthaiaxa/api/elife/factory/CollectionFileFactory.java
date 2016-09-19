package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.service.RLSService;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class CollectionFileFactory {
    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private QuoteService quoteService;

    @Inject
    private PolicyService policyService;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    /**
     * The structure of Collection import file must be matched with structure inside {@link RLSService#importCollectionFile(InputStream)}
     *
     * @param policyNumbers
     * @return
     */
    public static InputStream initCollectionExcelFile(String... policyNumbers) {
//        File file = IOUtil.createFile(fileName);
//        InputStream is = IOUtil.getInputStream(file);

        HSSFWorkbook workbook = null;
//        try {
        workbook = new HSSFWorkbook();
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

//            ExcelUtils.createCells(headerRow, 1, ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_2));
//            ExcelUtils.createCells(headerRow, 2, ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_3));
//            ExcelUtils.createCells(headerRow, 3, ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_4));
//            ExcelUtils.createCells(headerRow, 4, ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_5));
//            ExcelUtils.createCells(headerRow, 5, ExcelUtils.text(RLSService.COLLECTION_FILE_COLUMN_NAME_6));

//        } catch (IOException e) {
//            throw new FileIOException("Cannot load HSSFWorkbook", e);
//        }

//        CollectionFile collectionFile = new CollectionFile();

    }
//
//    public static InputStream collectionFileLine(Policy policy) {
//
//    }

}
