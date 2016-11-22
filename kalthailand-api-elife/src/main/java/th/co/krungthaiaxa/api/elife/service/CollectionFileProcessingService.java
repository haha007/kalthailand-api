package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.action.ActionWithResult;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.LogUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.data.DeductionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptNumber;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptService;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static th.co.krungthaiaxa.api.elife.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.api.elife.service.LineService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
import static th.co.krungthaiaxa.api.elife.service.LineService.RESPONSE_CODE_SUCCESS;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.appendRow;
import static th.co.krungthaiaxa.api.elife.utils.ExcelUtils.text;

/**
 * This class contains methods for processing the collectionFile.
 * After processing collection files, the result (Excel) file will be updated into RLS system (manually).
 * Collection always process only the 'renewal' payments. The 'new business' payments were already handled by FE.
 */
@Service
public class CollectionFileProcessingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CollectionFileProcessingService.class);
    /**
     * For processing collection file, it's always Renewal payment, so the NewBusiness is always false.
     */
    private static final boolean NEW_BUSINESS = false;

    public final static String COLLECTION_FILE_SHEET_NAME = "LFDISC6";
    private final static Integer COLLECTION_FILE_NUMBER_OF_COLUMNS = 6;
    public final static String COLLECTION_FILE_COLUMN_NAME_1 = "M92DOC6";
    public final static String COLLECTION_FILE_COLUMN_NAME_2 = "M92BANK6";
    public final static String COLLECTION_FILE_COLUMN_NAME_3 = "M92BKCD6";
    public final static String COLLECTION_FILE_COLUMN_NAME_4 = "M92PNO6";
    public final static String COLLECTION_FILE_COLUMN_NAME_5 = "M92PMOD6";
    public final static String COLLECTION_FILE_COLUMN_NAME_6 = "M92PRM6";
    public static final String ERROR_NO_REGISTRATION_KEY_FOUND = "No registration key found to process the payment. Payment is not successful";

    private final CollectionFileRepository collectionFileRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;
    private final PaymentService paymentService;
    private final EreceiptService ereceiptService;

    //    private final DeductionFileRepository deductionFileRepository;
    /**
     * Don't put final here because we need inject mock test dependency
     */
    private LineService lineService;
    private final PaymentFailEmailService paymentRetryEmailService;
    private final PaymentFailLineNotificationService paymentRetryLineNotificationService;

    @Inject
    public CollectionFileProcessingService(CollectionFileRepository collectionFileRepository, PaymentRepository paymentRepository, PolicyRepository policyRepository, PolicyService policyService, PaymentService paymentService, EreceiptService ereceiptService,
            LineService lineService,
            PaymentFailEmailService paymentRetryEmailService, PaymentFailLineNotificationService paymentRetryLineNotificationService) {
        this.collectionFileRepository = collectionFileRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyService = policyService;
        this.paymentService = paymentService;
        this.ereceiptService = ereceiptService;
//        this.deductionFileRepository = deductionFileRepository;
        this.lineService = lineService;
        this.paymentRetryEmailService = paymentRetryEmailService;
        this.paymentRetryLineNotificationService = paymentRetryLineNotificationService;
    }

    public static final Function<PeriodicityCode, String> PAYMENT_MODE = periodicityCode -> {
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

    public synchronized CollectionFile importCollectionFile(InputStream inputStream) {
        CollectionFile collectionFile = readCollectionExcelFile(inputStream);
        validateNotDuplicatePolicies(collectionFile);
        collectionFile.getLines().forEach(this::importCollectionFileLine);
        return collectionFileRepository.save(collectionFile);
    }

    public CollectionFile findOne(String collectionFileId) {
        return collectionFileRepository.findOne(collectionFileId);
    }

    public List<CollectionFile> getCollectionFiles() {
        return collectionFileRepository.findAll(new Sort(Sort.Direction.DESC, "receivedDate"));
    }

    @Scheduled(cron = "0 0 9-17 * * ?")
    public void processLatestCollectionFilesJob() {
        processLatestCollectionFiles();
    }

    /**
     * This method must be synchronized to avoid process one collectionFile many times by multi-submit.
     *
     * @return the collection files which has just processed.
     */
    public synchronized List<CollectionFile> processLatestCollectionFiles() {
        Instant startTime = LogUtil.logStarting("Process collection files [start]");
        List<CollectionFile> collectionFiles = collectionFileRepository.findByJobStartedDateNull();
        LOGGER.info("Found [" + collectionFiles.size() + "] collection files to process.");
        for (CollectionFile collectionFile : collectionFiles) {
            processCollectionFile(collectionFile);
        }
        LogUtil.logRuntime(startTime, "Process collection files [finished]");
        return collectionFiles;
    }

    private void processCollectionFile(CollectionFile collectionFile) {
        try {
            collectionFile.setJobStartedDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
            DeductionFile deductionFile = new DeductionFile();
            collectionFile.setDeductionFile(deductionFile);
            LOGGER.info("Found [" + collectionFile.getLines().size() + "] collection(s) line(s) to process.");
            for (CollectionFileLine collectionFileLine : collectionFile.getLines()) {
                processCollectionFileLine(deductionFile, collectionFileLine);
            }
            collectionFile.setJobEndedDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
            collectionFileRepository.save(collectionFile);
        } catch (Exception ex) {
            LOGGER.error("Error while process collection file: " + ex.getMessage() + " \nCollectionFile: " + ObjectMapperUtil.toStringMultiLine(collectionFile), ex);
        }
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

    private CollectionFile readCollectionExcelFile(InputStream excelInputStream) {
        notNull(excelInputStream, "The excel file is not available");
        try (Workbook workbook = WorkbookFactory.create(excelInputStream)) {

            // check if sheet is found
            Sheet sheet = workbook.getSheet(COLLECTION_FILE_SHEET_NAME);
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
            int rowId = 0;
            while (rowIterator.hasNext()) {
                rowId++;
                Row currentRow = rowIterator.next();
                String collectionDate = ExcelUtils.getCellValueAsString(currentRow.getCell(0));
                String collectionBank = ExcelUtils.getCellValueAsString(currentRow.getCell(1));
                String bankCode = ExcelUtils.getCellValueAsString(currentRow.getCell(2));
                String policyNumber = ExcelUtils.getCellValueAsString(currentRow.getCell(3));
                String paymentMode = ExcelUtils.getCellValueAsString(currentRow.getCell(4));
                Double premiumAmount = ExcelUtils.getCellValueAsDouble(currentRow.getCell(5));

                if (StringUtils.isBlank(collectionDate) || StringUtils.isBlank(collectionBank) || StringUtils.isBlank(bankCode) || StringUtils.isBlank(policyNumber) || StringUtils.isBlank(paymentMode) || premiumAmount == null) {
                    LOGGER.warn("Ignore the row[{}] because there's not enough information.", rowId);
                    continue;
                }

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
        } catch (InvalidFormatException | IOException e) {
            throw new IllegalArgumentException("Unable to read the excel file: " + e.getMessage(), e);
        }
    }

    private void importCollectionFileLine(CollectionFileLine collectionFileLine) {
        LOGGER.info("Import collectionFileLine [start]: policyNumber: {}", collectionFileLine.getPolicyNumber());
        String policyId = collectionFileLine.getPolicyNumber();
        isTrue(StringUtils.isNotBlank(policyId), "policyNumber must be notempty: " + ObjectMapperUtil.toStringMultiLine(collectionFileLine));
        Policy policy = policyService.validateExistPolicy(policyId);
        isTrue(policy.getStatus().equals(PolicyStatus.VALIDATED), "The policy [" + collectionFileLine.getPolicyNumber() + "] has not been validated and payments can't go through without validation");
        validatePaymentModeWithAtpEnable(collectionFileLine, policy);

        // 28 is the maximum nb of days between a scheduled payment and collection file first cycle start date
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayMinus28Days = now.minusDays(28);
        LocalDateTime tomorrow = now.plusDays(1);

        // There should be a scheduled payment for which due date is within the last 28 days
        Optional<Payment> notProcessedPaymentInThisMonth = paymentRepository.findOneByPolicyIdAndDueDateRangeAndInStatus(policyId, todayMinus28Days, tomorrow, PaymentStatus.NOT_PROCESSED);
        if (notProcessedPaymentInThisMonth.isPresent()) {
            collectionFileLine.setPaymentId(notProcessedPaymentInThisMonth.get().getPaymentId());
            LOGGER.info("Existing payment id [" + notProcessedPaymentInThisMonth.get().getPaymentId() + "] has been added for the " +
                    "collection file line about policy [" + policy.getPolicyId() + "] ");
            return;
        }

        // If payment isn't found, Collection file "always win", and the payment received in the collection has to go through
        // But for the payment to go through, we need to find the registration key that was used previously
        String lastRegistrationKey = findLastRegistrationKey(policyId);

        //Create the predefined payment. The user has not really payed yet. That's why it doesn't have effective date.
        Payment newPayment = new Payment(policy.getPolicyId(),
                collectionFileLine.getPremiumAmount(),
                policy.getCommonData().getProductCurrency(),
                DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        if (StringUtils.isBlank(lastRegistrationKey)) {
            LOGGER.info("Unable to find a schedule payment for policy [" + policy.getPolicyId() + "] and a " +
                    "previously used registration key, will create one payment from scratch, but payment will fail " +
                    "since it has no registration key");
        } else {
            newPayment.setRegistrationKey(lastRegistrationKey);
            LOGGER.info("Unable to find a schedule payment for policy [" + policy.getPolicyId() + "], will " +
                    "create one from scratch");
        }
        paymentRepository.save(newPayment);
        policy.addPayment(newPayment);
        policy.setLastUpdateDateTime(Instant.now());
        policyRepository.save(policy);
        collectionFileLine.setPaymentId(newPayment.getPaymentId());
        LOGGER.info("Import collectionFileLine [finished]: policyNumber: {}, paymentId: {}", collectionFileLine.getPolicyNumber(), collectionFileLine.getPaymentId());
    }

    private void validatePaymentModeWithAtpEnable(CollectionFileLine collectionFileLine, Policy policy) {
        PeriodicityCode policyPeriodicityCode = ProductUtils.validateExistPeriodicityCode(policy);
        String policyPaymentPeriodicityCode = PAYMENT_MODE.apply(policyPeriodicityCode);
        if (!policyPaymentPeriodicityCode.equalsIgnoreCase(collectionFileLine.getPaymentMode())) {
            String msg = String.format("The payment mode in policy and payment mode in collection file is not match: policyID: %s, paymentMode: %s vs. collection's paymentMode: %s", policy.getPolicyId(), policyPaymentPeriodicityCode, collectionFileLine.getPaymentMode());
            throw new BadArgumentException(msg);
        }
        if (!ProductUtils.isAtpModeEnable(policy)) {
            String msg = String.format("The policy doesn't have ATP mode: policyID: %s, paymentMode: %s", policy.getPolicyId(), policyPaymentPeriodicityCode);
            throw new BadArgumentException(msg);
        }
    }

    /**
     * @param collectionFile
     * @return key: policyNumber, value: counts of duplicated lines
     */
    private Map<String, Integer> validateNotDuplicatePolicies(CollectionFile collectionFile) {
        Map<String, Integer> duplicatedLines = new HashMap<>();
        CollectionFileLine[] lines = collectionFile.getLines().toArray(new CollectionFileLine[0]);//Convert to Array to improve performance.
        for (int i = 0; i < lines.length; i++) {
            CollectionFileLine iline = lines[i];
            String ipolicyNumber = iline.getPolicyNumber();
            if (StringUtils.isNotBlank(ipolicyNumber)) {
                for (int j = i + 1; j < lines.length; j++) {
                    CollectionFileLine jline = lines[j];
                    if (ipolicyNumber.equals(jline.getPolicyNumber())) {
                        Integer count = duplicatedLines.get(ipolicyNumber);
                        if (count == null) {
                            count = 1;
                        }
                        count++;
                        duplicatedLines.put(ipolicyNumber, count);
                    }
                }
            }
        }
        if (duplicatedLines.size() > 0) {
            throw new BadArgumentException("Duplicated policies " + duplicatedLines.keySet() + "", duplicatedLines, ErrorCode.DETAILS_TYPE_DUPLICATE);
        }
        return duplicatedLines;
    }

    private String findLastRegistrationKey(String policyNumber) {
        Optional<Payment> paymentOptional = paymentService.findLastestPaymentByPolicyNumberAndRegKeyNotNull(policyNumber);
        if (paymentOptional.isPresent()) {
            return paymentOptional.get().getRegistrationKey();
        } else {
            return null;
        }
    }

    private void processCollectionFileLine(DeductionFile deductionFile, CollectionFileLine collectionFileLine) {
        boolean newBusiness = NEW_BUSINESS;
        LOGGER.info("Processing collectionFileLine [start]: policyNumber: {}", collectionFileLine.getPolicyNumber());

        String paymentId = collectionFileLine.getPaymentId();
        String policyId = collectionFileLine.getPolicyNumber();
        Double premiumAmount = collectionFileLine.getPremiumAmount();
        Policy policy = null;
        Payment payment = null;
        String currencyCode = null;
        String paymentModeString = null;
        String resultMessage = "CollectionFileLine is not processed completely yet: " + ObjectMapperUtil.toStringMultiLine(collectionFileLine);
        String resultCode = RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
        try {
            policy = policyRepository.findByPolicyId(policyId);
            if (policy == null) {
                throw new UnexpectedException("Not found policy " + policyId);
            }
            PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
            paymentModeString = PAYMENT_MODE.apply(periodicityCode);
            String productId = policy.getCommonData().getProductId();

            payment = paymentRepository.findOne(paymentId);
            if (payment == null) {
                throw new UnexpectedException("Not found payment " + paymentId);
            }
            currencyCode = payment.getAmount().getCurrencyCode();
            String orderId = "R-" + payment.getPolicyId() + "-" + (new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));

            String lastRegistrationKey = findLastRegistrationKey(payment.getPolicyId());
            if (StringUtils.isBlank(lastRegistrationKey)) {
                throw new UnexpectedException("Not found registrationKey for policy " + policyId + ", paymentId " + paymentId);
            }
            LinePayRecurringResponse linePayResponse = lineService.preApproved(lastRegistrationKey, premiumAmount, currencyCode, productId, orderId);
            resultCode = linePayResponse.getReturnCode();
            resultMessage = linePayResponse.getReturnMessage();
            //TODO need to recheck with business team
            if (Math.abs(premiumAmount - payment.getAmount().getValue()) >= 1) {
                String msg = String.format("The money in collection file %s is not match with the predefined amount %s", premiumAmount, payment.getAmount());
                throw new UnexpectedException(msg);
            }
            payment.getAmount().setValue(premiumAmount);
            payment.setOrderId(orderId);
            if (LineService.RESPONSE_CODE_SUCCESS.equals(resultCode)) {
                //Only generate new ereceiptNumber when payment success.
                EreceiptNumber ereceiptNumber = ereceiptService.generateEreceiptFullNumber(newBusiness);
                payment.setReceiptNumber(ereceiptNumber);
                payment.setNewBusiness(newBusiness);
                payment.setReceiptNumberOldPattern(false);
            }
            paymentService.updateByLinePayResponse(payment, linePayResponse);
        } catch (Exception ex) {
            LOGGER.error("Error when process collection line: " + ex.getMessage() + ". Collection line:%n" + ObjectMapperUtil.toStringMultiLine(collectionFileLine), ex);
            resultCode = RESPONSE_CODE_ERROR_INTERNAL_LINEPAY;
            resultMessage = ex.getMessage();
            if (payment != null) {
                paymentService.updatePaymentWithErrorStatus(payment, premiumAmount, currencyCode, LINE, resultCode, resultMessage);
            }
        } finally {
            DeductionFileLine deductionFileLine = initDeductionFileLine(collectionFileLine, paymentModeString, resultCode, resultMessage);
            deductionFile.addLine(deductionFileLine);
            if (hasErrorButNotInternalErrorWhenCallLinePay(resultCode)) {
                if (policy == null || policy.getPolicyId() == null) {
                    LOGGER.warn("Cannot find policyId " + policyId + ", so cannot get information of insured person. Therefore we cannot send inform email to insured customer.");
                } else {
                    setResultOfFailPaymentNotificationToDeductionFileLine(deductionFileLine, policy, payment);
                }
            }
        }
        LOGGER.info("Process collectionFileLine [finished]: policyNumber: {}, paymentId: {}", collectionFileLine.getPolicyNumber(), collectionFileLine.getPaymentId());
    }

    //TODO need to update the list of internal error. E.g: 1106: our server cannot connect to LINE server: should not send email to client.
    private boolean hasErrorButNotInternalErrorWhenCallLinePay(String resultCode) {
        return !resultCode.equals(RESPONSE_CODE_SUCCESS) && !LineService.RESPONSE_CODES_ERROR_BY_INTERNAL_APP.contains(resultCode);
    }

    private void setResultOfFailPaymentNotificationToDeductionFileLine(DeductionFileLine deductionFileLine, Policy policy, Payment payment) {
        String informCustomerCode;
        String informCustomerMessage;
        List<ActionWithResult> notifyActions = new ArrayList<>();
        notifyActions.add(new ActionWithResult("Email") {
            @Override
            public void executeSuccess() {
                paymentRetryEmailService.sendEmail(policy, payment);
                this.resultCode = PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS;
                this.resultMessage = "Sent inform email to insured person";
            }
        });
        notifyActions.add(new ActionWithResult("Line notification") {
            @Override
            public void executeSuccess() {
                paymentRetryLineNotificationService.sendNotification(policy, payment);
                this.resultCode = PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS;
                this.resultMessage = "Sent inform line notification to insured person";
            }
        });
        notifyActions.stream().forEach(actionWithResult -> actionWithResult.execute());
        informCustomerCode = notifyActions.stream().map(notifyAction -> notifyAction.getActionName() + ": " + notifyAction.getResultCode()).collect(Collectors.joining(", "));
        informCustomerMessage = notifyActions.stream().map(notifyAction -> notifyAction.getResultMessage()).collect(Collectors.joining(", "));
        deductionFileLine.setInformCustomerCode(informCustomerCode);
        deductionFileLine.setInformCustomerMessage(informCustomerMessage);
    }

    private DeductionFileLine initDeductionFileLine(CollectionFileLine collectionFileLine, String paymentMode, String errorCode, String errorMessage) {

        DeductionFileLine deductionFileLine = new DeductionFileLine();
        deductionFileLine.setPaymentId(collectionFileLine.getPaymentId());
        deductionFileLine.setAmount(collectionFileLine.getPremiumAmount());
        deductionFileLine.setBankCode(collectionFileLine.getBankCode());
        deductionFileLine.setPaymentMode(paymentMode);
        deductionFileLine.setPolicyNumber(collectionFileLine.getPolicyNumber());
        deductionFileLine.setProcessDate(LocalDateTime.now());
        deductionFileLine.setRejectionCode(errorCode);
        deductionFileLine.setRejectionMessage(errorMessage);
        return deductionFileLine;
    }

    private void createDeductionExcelFileLine(Sheet sheet, DeductionFileLine deductionFileLine) {
        appendRow(sheet,
                text(deductionFileLine.getPolicyNumber()),
                text(deductionFileLine.getBankCode()),
                text(deductionFileLine.getPaymentMode()),
                text(deductionFileLine.getAmount().toString()),
                text(ofPattern("yyyyMMdd").format(deductionFileLine.getProcessDate())),
                text((deductionFileLine.getRejectionCode().equals("0000") ? "" : deductionFileLine.getRejectionCode())));
    }

    public void setLineService(LineService lineService) {
        this.lineService = lineService;
    }

}
