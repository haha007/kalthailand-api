package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.model.PaymentInformation;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static java.time.LocalDate.now;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static th.co.krungthaiaxa.elife.api.utils.ExcelUtils.*;

@Service
public class RLSService {
    private final static Logger logger = LoggerFactory.getLogger(RLSService.class);
    private final static String COLLECTION_FILE_SHEET_NAME = "LFDISC6";
    private final static Integer COLLECTION_FILE_NUMBER_OF_COLUMNS = 6;
    private final static String COLLECTION_FILE_COLUMN_NAME_1 = "M92DOC6";
    private final static String COLLECTION_FILE_COLUMN_NAME_2 = "M92BANK6";
    private final static String COLLECTION_FILE_COLUMN_NAME_3 = "M92BKCD6";
    private final static String COLLECTION_FILE_COLUMN_NAME_4 = "M92PNO6";
    private final static String COLLECTION_FILE_COLUMN_NAME_5 = "M92PMOD6";
    private final static String COLLECTION_FILE_COLUMN_NAME_6 = "M92PRM6";

    private final CollectionFileRepository collectionFileRepository;

    private Function<PeriodicityCode, String> paymentMode = periodicityCode -> {
        if (periodicityCode.equals(PeriodicityCode.EVERY_YEAR)) {
            return "A";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_HALF_YEAR)) {
            return "S";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_QUARTER)) {
            return "Q";
        } else {
            return "M";
        }
    };

    @Inject
    public RLSService(CollectionFileRepository collectionFileRepository) {
        this.collectionFileRepository = collectionFileRepository;
    }

    public void importCollectionFile(InputStream is) {
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read the excel file", e);
        }
        // check if sheet is found
        HSSFSheet sheet = workbook.getSheet(COLLECTION_FILE_SHEET_NAME);
        notNull(sheet, "The file does not contain the sheet [" + COLLECTION_FILE_SHEET_NAME + "]");

        // check if right number of columns
        int noOfColumns = sheet.getRow(0).getLastCellNum();
        isTrue(noOfColumns == COLLECTION_FILE_NUMBER_OF_COLUMNS, "The file does not contain [" + COLLECTION_FILE_NUMBER_OF_COLUMNS + "] columns with data");

        // first line is does not contain data, but need to check for column header
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row firstRow = rowIterator.next();
        List<String> firstLine = new ArrayList<>();
        for (int i=0;i<COLLECTION_FILE_NUMBER_OF_COLUMNS;i++) {
            firstLine.add(firstRow.getCell(i).getStringCellValue());
        }
        isTrue(firstLine.get(0).equals(COLLECTION_FILE_COLUMN_NAME_1), "The column #1 name is not [" + COLLECTION_FILE_COLUMN_NAME_1 + "]");
        isTrue(firstLine.get(1).equals(COLLECTION_FILE_COLUMN_NAME_2), "The column #2 name is not [" + COLLECTION_FILE_COLUMN_NAME_2 + "]");
        isTrue(firstLine.get(2).equals(COLLECTION_FILE_COLUMN_NAME_3), "The column #3 name is not [" + COLLECTION_FILE_COLUMN_NAME_3 + "]");
        isTrue(firstLine.get(3).equals(COLLECTION_FILE_COLUMN_NAME_4), "The column #4 name is not [" + COLLECTION_FILE_COLUMN_NAME_4 + "]");
        isTrue(firstLine.get(4).equals(COLLECTION_FILE_COLUMN_NAME_5), "The column #5 name is not [" + COLLECTION_FILE_COLUMN_NAME_5 + "]");
        isTrue(firstLine.get(5).equals(COLLECTION_FILE_COLUMN_NAME_6), "The column #6 name is not [" + COLLECTION_FILE_COLUMN_NAME_6 + "]");

        // copy all the lines
        CollectionFile collectionFile = new CollectionFile();
        collectionFile.setSendDate(now());
        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            List<String> line = new ArrayList<>();
            for (int i=0;i<COLLECTION_FILE_NUMBER_OF_COLUMNS;i++) {
                line.add(getCellValueAsString(currentRow.getCell(i)));
            }
            collectionFile.addLine(line);
        }
        collectionFile.setFileHashCode(collectionFile.calculateFileHashCode());
        CollectionFile previousFile = collectionFileRepository.findByFileHashCode(collectionFile.getFileHashCode());
        if (previousFile != null) {
            throw new IllegalArgumentException("The file has already been uploaded");
        }

        collectionFileRepository.save(collectionFile);
    }

    public byte[] exportPayments(List<Pair<Policy, PaymentInformation>> payments) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LFPATPTDR6");
        appendRow(sheet, //
                text("M93RPNO6"), // Policy ID
                text("M93RBKCD6"), // Bank Code
                text("M93RPMOD6"), // Payment Mode
                text("M93RPRM6"), // Deposit amount
                text("M93RDOC6"), // Process date
                text("M93RJCD6")); // Rejection Code
        payments.stream().forEach(data -> exportPayment(sheet, data.getLeft(), data.getRight()));
        autoWidthAllColumns(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Unable to write content of excel file", e);
            throw new IllegalStateException("Unable to write content of excel file", e);
        }
    }

    private void exportPayment(Sheet sheet, Policy policy, PaymentInformation paymentInformation) {
        appendRow(sheet,
                text(policy.getPolicyId()),
                empty(),
                text(paymentMode.apply(policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode())),
                text(paymentInformation.getAmount().getValue().toString()),
                time(paymentInformation.getDate()),
                text(paymentInformation.getRejectionErrorMessage()));
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:return String.valueOf(cell.getNumericCellValue());
            default: return null;
        }
    }
}