package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class CollectionFileFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(CollectionFileFactory.class);
    public static final double DEFAULT_PAYMENT_MONEY = 2000.0;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private PolicyService policyService;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    /**
     * The structure of Collection import file must be matched with structure inside {@link CollectionFileProcessingService#importCollectionFile(InputStream)}
     *
     * @param collectionFileLineInputs
     * @return the inputstream of Excel file which contains policyNumbers need to be processed.
     */
    public static InputStream constructCollectionExcelFileWithDefaultPayment(CollectionFileLine... collectionFileLineInputs) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(CollectionFileProcessingService.COLLECTION_FILE_SHEET_NAME);
        ExcelUtils.appendRow(
                sheet,
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_1),
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_2),
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_3),
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_4),
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_5),
                ExcelUtils.text(CollectionFileProcessingService.COLLECTION_FILE_COLUMN_NAME_6)
        );
        for (CollectionFileLine collectionFileLineInput : collectionFileLineInputs) {
            Double paymentValue = collectionFileLineInput.getPremiumAmount() == null ? DEFAULT_PAYMENT_MONEY : collectionFileLineInput.getPremiumAmount();
            ExcelUtils.appendRow(
                    sheet,
                    ExcelUtils.text(collectionFileLineInput.getCollectionDate()),
                    ExcelUtils.text(collectionFileLineInput.getCollectionBank()),
                    ExcelUtils.text(collectionFileLineInput.getBankCode()),
                    ExcelUtils.text(collectionFileLineInput.getPolicyNumber()),
                    ExcelUtils.text(collectionFileLineInput.getPaymentMode()),
                    ExcelUtils.integer(paymentValue)
            );
        }
        byte[] bytes = ExcelIOUtils.writeToBytes(workbook);
        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(bytes);
        File file = new File(TestUtil.PATH_TEST_RESULT + "collection-file/LFDISC6_" + DateTimeUtil.formatNowForFilePath() + ".xls");
        IOUtil.writeInputStream(file, fileInputStream);
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream constructCollectionExcelFileWithDefaultPayment(String... policyNumbers) {
        CollectionFileLine[] collectionFileLines = toCollectionFileLineWithDefaultPaymentValue(policyNumbers);
        return constructCollectionExcelFileWithDefaultPayment(collectionFileLines);
    }

    public static InputStream constructCollectionExcelFileByPolicy(Policy... policy) {
        CollectionFileLine[] collectionFileLines = toCollectionFileLine(policy);
        return constructCollectionExcelFileWithDefaultPayment(collectionFileLines);
    }

    private static CollectionFileLine constructCollectionFileLine(String policyNumber, Double paymentValue, String paymentMode) {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setCollectionDate(DateTimeUtil.formatLocalDate(LocalDate.now(), "yyyyMMdd"));
        collectionFileLine.setCollectionBank("L");
        collectionFileLine.setBankCode("111");
        collectionFileLine.setPolicyNumber(policyNumber);
        collectionFileLine.setPaymentMode(paymentMode);
        collectionFileLine.setPremiumAmount(paymentValue);
        return collectionFileLine;
    }

    private static CollectionFileLine[] toCollectionFileLineWithDefaultPaymentValue(String... policyNumbers) {
        List<CollectionFileLine> collectionFileLineInputList = Arrays.asList(policyNumbers).stream().map(
                policyNumber -> constructCollectionFileLine(policyNumber, DEFAULT_PAYMENT_MONEY, "M")
        ).collect(Collectors.toList());
        return collectionFileLineInputList.toArray(new CollectionFileLine[0]);
    }

    private static CollectionFileLine[] toCollectionFileLine(Policy... policies) {
        List<CollectionFileLine> collectionFileLineInputList = Arrays.asList(policies).stream().map(
                policy -> constructCollectionFileLine(policy.getPolicyId(), policy.getPayments().get(0).getAmount().getValue(), getPaymentMode(policy))
        ).collect(Collectors.toList());
        return collectionFileLineInputList.toArray(new CollectionFileLine[0]);
    }

    private static final String getPaymentMode(Policy policy) {
        PeriodicityCode periodicityCode = policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode();
        return CollectionFileProcessingService.PAYMENT_MODE.apply(periodicityCode);
    }
}
