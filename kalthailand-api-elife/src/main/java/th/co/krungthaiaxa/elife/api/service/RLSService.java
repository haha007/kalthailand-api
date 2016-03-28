package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.data.CollectionFileLine;
import th.co.krungthaiaxa.elife.api.data.DeductionFile;
import th.co.krungthaiaxa.elife.api.data.DeductionFileLine;
import th.co.krungthaiaxa.elife.api.model.Payment;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.time.LocalDate.now;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneId.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.NOT_PROCESSED;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.VALIDATED;
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
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;

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
    public RLSService(CollectionFileRepository collectionFileRepository, PaymentRepository paymentRepository, PolicyRepository policyRepository, PolicyService policyService) {
        this.collectionFileRepository = collectionFileRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyService = policyService;
    }

    public void importCollectionFile(InputStream is) {
        CollectionFile collectionFile = readCollectionExcelFile(is);
        collectionFile.getLines().forEach(this::addPaymentId);
        collectionFileRepository.save(collectionFile);
    }

    public CollectionFile findOne(String collectionFileId) {
        return collectionFileRepository.findOne(collectionFileId);
    }

    public List<CollectionFile> getCollectionFiles() {
        return collectionFileRepository.findAll(new Sort(Sort.Direction.DESC, "receivedDate"));
    }

    public void processLatestCollectionFile() {
        List<CollectionFile> collectionFiles = collectionFileRepository.findByJobStartedDateNull();
        for (CollectionFile collectionFile : collectionFiles) {
            collectionFile.setJobStartedDate(LocalDateTime.now(of(SHORT_IDS.get("VST"))));
            DeductionFile deductionFile = new DeductionFile();
            collectionFile.setDeductionFile(deductionFile);
            for (CollectionFileLine collectionFileLine : collectionFile.getLines()) {
                // TODO : call LINE Pay API

                // TODO : error code is coming from LINE Pay
                deductionFile.addLine(getDeductionFileLine(collectionFileLine, ""));
            }
            collectionFile.setJobEndedDate(LocalDateTime.now(of(SHORT_IDS.get("VST"))));
            collectionFile.setDeductionFile(deductionFile);
            collectionFileRepository.save(collectionFile);
        }
    }

    public byte[] createDeductionExcelFile(DeductionFile deductionFile) {
        notNull(deductionFile, "No deduction file has been created");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LFPATPTDR6");
        appendRow(sheet, //
                text("M93RPNO6"), // Policy ID
                text("M93RBKCD6"), // Bank Code
                text("M93RPMOD6"), // Payment Mode
                text("M93RPRM6"), // Deposit amount
                text("M93RDOC6"), // Process date
                text("M93RJCD6")); // Rejection Code
        deductionFile.getLines().stream().forEach(deductionFileLine -> createDeductionExcelFileLine(sheet, deductionFileLine));
        autoWidthAllColumns(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }
    }

    CollectionFile readCollectionExcelFile(InputStream is) {
        notNull(is, "The file excel file is not available");
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

        // first line does not contain data, but need to check for column header
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row firstRow = rowIterator.next();
        List<String> firstLine = new ArrayList<>();
        for (int i = 0; i < COLLECTION_FILE_NUMBER_OF_COLUMNS; i++) {
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
        collectionFile.setReceivedDate(LocalDateTime.now());
        StringBuilder stringBuilder = new StringBuilder();
        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            String collectionDate = getCellValueAsString(currentRow.getCell(0));
            String collectionBank = getCellValueAsString(currentRow.getCell(1));
            String bankCode = getCellValueAsString(currentRow.getCell(2));
            String policyNumber = getCellValueAsString(currentRow.getCell(3));
            String paymentMode = getCellValueAsString(currentRow.getCell(4));
            Double premiumAmount = getCellValueAsDouble(currentRow.getCell(5));

            stringBuilder.append(collectionDate);
            stringBuilder.append(collectionBank);
            stringBuilder.append(bankCode);
            stringBuilder.append(policyNumber);
            stringBuilder.append(paymentMode);
            stringBuilder.append(premiumAmount);

            CollectionFileLine collectionFileLine = new CollectionFileLine();
            collectionFileLine.setCollectionDate(collectionDate);
            collectionFileLine.setCollectionBank(collectionBank);
            collectionFileLine.setBankCode(bankCode);
            collectionFileLine.setPolicyNumber(policyNumber);
            collectionFileLine.setPaymentMode(paymentMode);
            collectionFileLine.setPremiumAmount(premiumAmount);
            collectionFile.addLine(collectionFileLine);
        }
        String sha256 = sha256Hex(stringBuilder.toString());
        collectionFile.setFileHashCode(sha256);
        CollectionFile previousFile = collectionFileRepository.findByFileHashCode(collectionFile.getFileHashCode());
        if (previousFile != null) {
            throw new IllegalArgumentException("The file has already been uploaded");
        }

        return collectionFile;
    }

    void addPaymentId(CollectionFileLine collectionFileLine) {
        Optional<Policy> policy = policyService.findPolicy(collectionFileLine.getPolicyNumber());
        isTrue(policy.isPresent(), "Unable to find a policy [" + collectionFileLine.getPolicyNumber() + "]");
        isTrue(policy.get().getStatus().equals(VALIDATED), "The policy [" +
                collectionFileLine.getPolicyNumber() + "] has not been validated and payments can't go through without validation");
        isTrue(policy.get().getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH),
                "Policy [" + collectionFileLine.getPolicyNumber() + "] is not a monthly payment policy");

        // 28 is the maximum nb of days between a scheduled payment and collection file first cycle start date
        LocalDate now = now();
        LocalDate todayMinus28Days = now.minusDays(28);

        // There should be a scheduled payment for which due date is within the last 28 days
        Optional<Payment> payment = policy.get().getPayments()
                .stream()
                .filter(tmp -> tmp.getStatus().equals(NOT_PROCESSED))
                .filter(tmp -> tmp.getDueDate().isAfter(todayMinus28Days))
                .filter(tmp -> tmp.getDueDate().isBefore(now.plusDays(1))) // plus 1 day because a.isBefore(a) = false
                .findFirst();

        if (!payment.isPresent()) {
            // If payment isn't found, Collection file "always win", and the payment received in the collection has to go through anyway
            logger.info("Unable to find a schedule payment for policy [" + policy.get().getPolicyId() + "], will create one from scratch");
            Payment newPayment = new Payment(collectionFileLine.getPremiumAmount(), "THB", LocalDate.now(of(SHORT_IDS.get("VST"))));
            paymentRepository.save(newPayment);
            policy.get().addPayment(newPayment);
            policyRepository.save(policy.get());
            collectionFileLine.setPaymentId(newPayment.getPaymentId());
        }
        else {
            collectionFileLine.setPaymentId(payment.get().getPaymentId());
        }
    }

    private DeductionFileLine getDeductionFileLine(CollectionFileLine collectionFileLine, String errorCode) {
        Optional<Policy> policy = policyService.findPolicy(collectionFileLine.getPolicyNumber());
        isTrue(policy.isPresent(), "Unable to find a policy [" + collectionFileLine.getPolicyNumber() + "]");

        PeriodicityCode periodicityCode = policy.get().getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        DeductionFileLine deductionFileLine = new DeductionFileLine();
        deductionFileLine.setAmount(collectionFileLine.getPremiumAmount());
        deductionFileLine.setBankCode(collectionFileLine.getBankCode());
        deductionFileLine.setPaymentMode(paymentMode.apply(periodicityCode));
        deductionFileLine.setPolicyNumber(collectionFileLine.getPolicyNumber());
        deductionFileLine.setProcessDate(LocalDateTime.now());
        deductionFileLine.setRejectionCode(errorCode);
        return deductionFileLine;
    }

    private void createDeductionExcelFileLine(Sheet sheet, DeductionFileLine deductionFileLine) {
        appendRow(sheet,
                text(deductionFileLine.getPolicyNumber()),
                text(deductionFileLine.getBankCode()),
                text(deductionFileLine.getPaymentMode()),
                text(deductionFileLine.getAmount().toString()),
                text(ofPattern("yyyyMMdd_hhmmss").format(deductionFileLine.getProcessDate())),
                text(deductionFileLine.getRejectionCode()));
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            default:
                return null;
        }
    }
}
