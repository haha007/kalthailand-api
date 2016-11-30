package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author khoi.tran on 11/30/16.
 */
@Service
public class CollectionFileImportingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CollectionFileImportingService.class);

    private final CollectionFileRepository collectionFileRepository;
    private final PaymentService paymentService;
    private final PolicyService policyService;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;

    @Autowired
    public CollectionFileImportingService(CollectionFileRepository collectionFileRepository, PaymentService paymentService, PolicyService policyService, PaymentRepository paymentRepository, PolicyRepository policyRepository) {
        this.collectionFileRepository = collectionFileRepository;
        this.paymentService = paymentService;
        this.policyService = policyService;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
    }

    public synchronized CollectionFile importCollectionFile(InputStream inputStream) {
        CollectionFile collectionFile = readCollectionExcelFile(inputStream);
        //We must allow duplicate policies.
//        validateNotDuplicatePolicies(collectionFile);
        collectionFile.getLines().forEach(this::importCollectionFileLine);
        return collectionFileRepository.save(collectionFile);
    }

    private CollectionFile readCollectionExcelFile(InputStream excelInputStream) {
        notNull(excelInputStream, "The excel file is not available");
        try (Workbook workbook = WorkbookFactory.create(excelInputStream)) {

            // check if sheet is found
            Sheet sheet = workbook.getSheet(CollectionFileService.COLLECTION_FILE_SHEET_NAME);
            notNull(sheet, "The file does not contain the sheet [" + CollectionFileService.COLLECTION_FILE_SHEET_NAME + "]");

            // check if right number of columns
            int noOfColumns = sheet.getRow(0).getLastCellNum();
            isTrue(noOfColumns == CollectionFileService.COLLECTION_FILE_NUMBER_OF_COLUMNS, "The file does not contain [" + CollectionFileService.COLLECTION_FILE_NUMBER_OF_COLUMNS + "] columns with data");

            // first line does not contain data, but need to check for column header
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row firstRow = rowIterator.next();
            List<String> firstLine = new ArrayList<>();
            for (int i = 0; i < CollectionFileService.COLLECTION_FILE_NUMBER_OF_COLUMNS; i++) {
                firstLine.add(firstRow.getCell(i).getStringCellValue());
            }
            isTrue(firstLine.get(0).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_1), "The column #1 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_1 + "]");
            isTrue(firstLine.get(1).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_2), "The column #2 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_2 + "]");
            isTrue(firstLine.get(2).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_3), "The column #3 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_3 + "]");
            isTrue(firstLine.get(3).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_4), "The column #4 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_4 + "]");
            isTrue(firstLine.get(4).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_5), "The column #5 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_5 + "]");
            isTrue(firstLine.get(5).equals(CollectionFileService.COLLECTION_FILE_COLUMN_NAME_6), "The column #6 name is not [" + CollectionFileService.COLLECTION_FILE_COLUMN_NAME_6 + "]");

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
        String lastRegistrationKey = paymentService.findLastRegistrationKey(policyId);

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
        String policyPaymentPeriodicityCode = CollectionFileService.PAYMENT_MODE.apply(policyPeriodicityCode);
        if (!policyPaymentPeriodicityCode.equalsIgnoreCase(collectionFileLine.getPaymentMode())) {
            String msg = String.format("The payment mode in policy and payment mode in collection file is not match: policyID: %s, paymentMode: %s vs. collection's paymentMode: %s", policy.getPolicyId(), policyPaymentPeriodicityCode, collectionFileLine.getPaymentMode());
            throw new BadArgumentException(msg);
        }
        if (!ProductUtils.isAtpModeEnable(policy)) {
            String msg = String.format("The policy doesn't have ATP mode: policyID: %s, paymentMode: %s", policy.getPolicyId(), policyPaymentPeriodicityCode);
            throw new BadArgumentException(msg);
        }
    }

}
