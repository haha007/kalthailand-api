package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.data.DeductionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.service.LineService.LINE_PAY_INTERNAL_ERROR;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.appendRow;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

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
    public static final String ERROR_NO_REGISTRATION_KEY_FOUND = "No registration key found to process the payment. Payment is not successful";

    private final CollectionFileRepository collectionFileRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;
    private LineService lineService;

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
    public RLSService(CollectionFileRepository collectionFileRepository, PaymentRepository paymentRepository, PolicyRepository policyRepository, PolicyService policyService, LineService lineService) {
        this.collectionFileRepository = collectionFileRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyService = policyService;
        this.lineService = lineService;
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

    
    //second, minute, hour, day of month, month, day(s) of week
    //@Scheduled(cron = "0 0 11 * * ?") ==> prd
    @Scheduled(cron = "* * */1 * * *")
    public void processLatestCollectionFile() {
        List<CollectionFile> collectionFiles = collectionFileRepository.findByJobStartedDateNull();       
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<< START PROCESS RECURRING PAYMENT WITH TIME ("+LocalDateTime.now(of(SHORT_IDS.get("VST")))+") >>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (CollectionFile collectionFile : collectionFiles) {        	
            logger.info("Found [" + collectionFiles.size() + "] collection(s) file to process.");
            collectionFile.setJobStartedDate(LocalDateTime.now(of(SHORT_IDS.get("VST"))));
            DeductionFile deductionFile = new DeductionFile();
            collectionFile.setDeductionFile(deductionFile);
            logger.info("Found [" + collectionFile.getLines().size() + "] collection(s) line(s) to process.");
            for (CollectionFileLine collectionFileLine : collectionFile.getLines()) {
                processCollectionFileLine(deductionFile, collectionFileLine);
            }
            collectionFile.setJobEndedDate(LocalDateTime.now(of(SHORT_IDS.get("VST"))));
            collectionFileRepository.save(collectionFile);
        }
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<< STOP PROCESS RECURRING PAYMENT WITH TIME ("+LocalDateTime.now(of(SHORT_IDS.get("VST")))+") >>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public byte[] createDeductionExcelFile(DeductionFile deductionFile) {
        notNull(deductionFile, "No deduction file has been created");
        notNull(deductionFile.getLines(), "No deduction file has been created");
        isTrue(deductionFile.getLines().size() != 0, "No deduction file has been created");

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
        ExcelUtils.autoWidthAllColumns(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write content of excel deduction file", e);
        }
    }

    CollectionFile readCollectionExcelFile(InputStream is) {
        notNull(is, "The excel file is not available");
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
            String collectionDate = ExcelUtils.getCellValueAsString(currentRow.getCell(0));
            String collectionBank = ExcelUtils.getCellValueAsString(currentRow.getCell(1));
            String bankCode = ExcelUtils.getCellValueAsString(currentRow.getCell(2));
            String policyNumber = ExcelUtils.getCellValueAsString(currentRow.getCell(3));
            String paymentMode = ExcelUtils.getCellValueAsString(currentRow.getCell(4));
            Double premiumAmount = ExcelUtils.getCellValueAsDouble(currentRow.getCell(5));

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
        isTrue(policy.get().getStatus().equals(PolicyStatus.VALIDATED), "The policy [" +
                collectionFileLine.getPolicyNumber() + "] has not been validated and payments can't go through without validation");
        isTrue(policy.get().getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(PeriodicityCode.EVERY_MONTH),
                "Policy [" + collectionFileLine.getPolicyNumber() + "] is not a monthly payment policy");

        // 28 is the maximum nb of days between a scheduled payment and collection file first cycle start date
        LocalDate now = now();
        LocalDate todayMinus28Days = now.minusDays(28);

        // There should be a scheduled payment for which due date is within the last 28 days
        Optional<Payment> payment = policy.get().getPayments()
                .stream()
                .filter(tmp -> tmp.getStatus().equals(PaymentStatus.NOT_PROCESSED))
                .filter(tmp -> tmp.getDueDate().isAfter(todayMinus28Days))
                .filter(tmp -> tmp.getDueDate().isBefore(now.plusDays(1))) // plus 1 day because a.isBefore(a) = false
                .findFirst();

        if (payment.isPresent()) {
            collectionFileLine.setPaymentId(payment.get().getPaymentId());
            logger.info("Existing payment id [" + payment.get().getPaymentId() + "] has been added for the " +
                    "collection file line about policy [" + policy.get().getPolicyId() + "] ");
            return;
        }

        // If payment isn't found, Collection file "always win", and the payment received in the collection has to go through
        // But for the payment to go through, we need to find the registration key that was used previously
        Optional<String> lastRegistrationKey = policy.get().getPayments()
                .stream()
                .filter(tmp -> tmp.getRegistrationKey() != null)
                .sorted((o1, o2) -> o1.getDueDate().compareTo(o2.getDueDate()))
                .map(Payment::getRegistrationKey)
                .findFirst();

        Payment newPayment = new Payment(policy.get().getPolicyId(),
                collectionFileLine.getPremiumAmount(),
                policy.get().getCommonData().getProductCurrency(),
                LocalDate.now(of(SHORT_IDS.get("VST"))));
        if (!lastRegistrationKey.isPresent()) {
            logger.info("Unable to find a schedule payment for policy [" + policy.get().getPolicyId() + "] and a " +
                    "previously used registration key, will create one payment from scratch, byt payment will fail " +
                    "since it has no registration key");
        } else {
            newPayment.setRegistrationKey(lastRegistrationKey.get());
            logger.info("Unable to find a schedule payment for policy [" + policy.get().getPolicyId() + "], will " +
                    "create one from scratch");
        }
        paymentRepository.save(newPayment);
        policy.get().addPayment(newPayment);
        policyRepository.save(policy.get());
        collectionFileLine.setPaymentId(newPayment.getPaymentId());
        logger.info("A new payment with id [" + newPayment.getPaymentId() + "] has been created and used for the " +
                "collection file line about policy [" + policy.get().getPolicyId() + "] ");
    }

    void processCollectionFileLine(DeductionFile deductionFile, CollectionFileLine collectionFileLine) {
        String paymentId = collectionFileLine.getPaymentId();
        Double amount = collectionFileLine.getPremiumAmount();
        Payment payment = paymentRepository.findOne(paymentId);

        if (payment.getRegistrationKey() == null) {
            // This should not happen since the registration key should always be found
            logger.error("No registration key was found in payment id [" + paymentId + "]");
            deductionFile.addLine(getDeductionFileLine(collectionFileLine, LINE_PAY_INTERNAL_ERROR));
            policyService.updatePaymentWithErrorStatus(payment, amount, payment.getAmount().getCurrencyCode(), LINE, LINE_PAY_INTERNAL_ERROR,
                    ERROR_NO_REGISTRATION_KEY_FOUND);
            return;
        }

        LinePayRecurringResponse linePayResponse;
        Policy policy = policyRepository.findByPolicyId(payment.getPolicyId());        	
    	String regKey = payment.getRegistrationKey();
    	Double amt = collectionFileLine.getPremiumAmount();
    	String currencyCode = payment.getAmount().getCurrencyCode();
    	String productId = policy.getCommonData().getProductId();
    	String orderId = "R-"+payment.getPolicyId()+"-"+(new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
    	/*System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
    	System.out.println("paymentId : "+paymentId);
    	System.out.println("regKey : " +regKey);
    	System.out.println("amt : " +amt);
    	System.out.println("currencyCode : " +currencyCode);
    	System.out.println("productId : " +productId);
    	System.out.println("orderId : " + orderId);
    	System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");*/
        try {        	
            linePayResponse = lineService.preApproved(regKey, amt, currencyCode,productId, orderId);
        } catch (IOException | RuntimeException e) {
            // An error occured while trying to contact LinePay
            logger.error("An error occured while trying to contact LinePay on payment id [" + paymentId + "]", e);
            deductionFile.addLine(getDeductionFileLine(collectionFileLine, LINE_PAY_INTERNAL_ERROR));
            policyService.updatePaymentWithErrorStatus(payment, amount, payment.getAmount().getCurrencyCode(), LINE, LINE_PAY_INTERNAL_ERROR,
                    "Error while contacting Line Pay API. Payment may be successful. Error is [" + e.getMessage() + "].");
            return;
        }

        deductionFile.addLine(getDeductionFileLine(collectionFileLine, linePayResponse.getReturnCode()));
        policyService.updateRecurringPayment(payment, amount, payment.getAmount().getCurrencyCode(), LINE, linePayResponse, paymentId,regKey,amt,productId,orderId);
        
        logger.info("Finished processing collection file line with payment id [" + paymentId + "]");
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
                text(ofPattern("yyyyMMdd").format(deductionFileLine.getProcessDate())),
                text((deductionFileLine.getRejectionCode().equals("0000")?"":deductionFileLine.getRejectionCode())));
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }
}
