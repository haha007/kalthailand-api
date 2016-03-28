package th.co.krungthaiaxa.elife.api.service;


import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.data.CollectionFileLine;
import th.co.krungthaiaxa.elife.api.data.DeductionFileLine;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.INCOMPLETE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.NOT_PROCESSED;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.VALIDATED;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RLSServiceTest {
    @Inject
    private PaymentRepository paymentRepository;
    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private RLSService rlsService;
    @Inject
    private CollectionFileRepository collectionFileRepository;
    @Inject
    private MongoTemplate mongoTemplate;

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(CollectionFile.class);
    }

    @Test
    public void should_not_save_collection_file_when_there_is_an_error() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(collectionFileRepository.findAll()).hasSize(0);
    }

    @Test
    public void should_throw_exception_when_file_not_valid_excel_file() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_not_found() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/something.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_extra_column() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_extraColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_sheet() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingSheet.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_column() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_wrong_column_name() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_wrongColumnName.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_saving_same_file_twice() {
        CollectionFile collectionFile = rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        collectionFileRepository.save(collectionFile);
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_save_empty_collection() {
        CollectionFile collectionFile = rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_empty.xls"));
        assertThat(collectionFile.getLines()).hasSize(0);
    }

    @Test
    public void should_save_collection_file_with_lines_hash_code_and_dates() {
        LocalDateTime now = LocalDateTime.now();
        CollectionFile collectionFile = rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        assertThat(collectionFile.getLines()).hasSize(50);
        assertThat(collectionFile.getFileHashCode()).isNotNull();
        assertThat(collectionFile.getReceivedDate()).isAfter(now);
        assertThat(collectionFile.getJobStartedDate()).isNull();
        assertThat(collectionFile.getJobEndedDate()).isNull();
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_does_not_exist() {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber("123");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_is_not_monthly() {
        Policy policy = getValidatedPolicy(EVERY_YEAR);
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_has_already_been_done() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(updatedPolicy.getPayments()).extracting("status").contains(NOT_PROCESSED);
        assertThat(updatedPolicy.getPayments()).extracting("amount").extracting("value").contains(100.0);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_due_date_is_older_than_28_days() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setDueDate(now().minusDays(30)));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(updatedPolicy.getPayments()).extracting("status").contains(NOT_PROCESSED);
        assertThat(updatedPolicy.getPayments()).extracting("amount").extracting("value").contains(100.0);
    }

    @Test
    public void should_find_a_payment_for_the_policy() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        assertThat(collectionFileLine.getPaymentId()).isNotNull();
    }

    @Test
    public void should_mark_collection_file_as_processed() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = collectionFileRepository.save(
                collectionFile(
                        collectionFileLine(policy, 100.0),
                        collectionFileLine(policy, 150.0),
                        collectionFileLine(policy, 200.0)
                ));
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        assertThat(updatedCollectionFile.getJobStartedDate()).isAfter(updatedCollectionFile.getReceivedDate());
        assertThat(updatedCollectionFile.getJobEndedDate()).isAfter(updatedCollectionFile.getJobStartedDate());
    }

    @Test
    public void should_process_collection_file() {
        Policy policy1 = getValidatedPolicy(EVERY_MONTH);
        Policy policy2 = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = collectionFileRepository.save(
                collectionFile(
                        collectionFileLine(policy1, 100.0),
                        collectionFileLine(policy2, 150.0),
                        collectionFileLine(policy2, 200.0)
                ));
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        List<DeductionFileLine> deductionFileLines = updatedCollectionFile.getDeductionFile().getLines();
        assertThat(deductionFileLines).extracting("policyNumber").containsExactly(policy1.getPolicyId(), policy2.getPolicyId(), policy2.getPolicyId());
        assertThat(deductionFileLines).extracting("bankCode").containsExactly("myBankCode", "myBankCode", "myBankCode");
        assertThat(deductionFileLines).extracting("paymentMode").containsExactly("M", "M", "M");
        assertThat(deductionFileLines).extracting("amount").containsExactly(100.0, 150.0, 200.0);
        assertThat(deductionFileLines).extracting("processDate").containsExactly(now(), now(), now());
        assertThat(deductionFileLines).extracting("rejectionCode").containsExactly("", "", "");
    }

    @Test
    public void should_not_create_deduction_file_line_when_no_deduction_line_created() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        CollectionFile collectionFile = collectionFile(collectionFileLine(policy, 100.0));
        assertThatThrownBy(() -> rlsService.createDeductionExcelFile(collectionFile.getDeductionFile()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_create_deduction_file_with_proper_header() throws IOException, InvalidFormatException {
        Policy policy = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = collectionFileRepository.save(
                collectionFile(
                        collectionFileLine(policy, 100.0)
                ));
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        byte[] excelFileContent = rlsService.createDeductionExcelFile(updatedCollectionFile.getDeductionFile());

        FileUtils.writeByteArrayToFile(new File("deductionFile.xlsx"), excelFileContent);
        assertThat(excelFileContent).isNotNull();
        Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelFileContent));
        assertThat(wb.getSheet("LFPATPTDR6")).isNotNull();
        assertThat(wb.getSheet("LFPATPTDR6").getLastRowNum()).isEqualTo(1);
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(0).getStringCellValue()).isEqualTo("M93RPNO6");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(1).getStringCellValue()).isEqualTo("M93RBKCD6");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(2).getStringCellValue()).isEqualTo("M93RPMOD6");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(3).getStringCellValue()).isEqualTo("M93RPRM6");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(4).getStringCellValue()).isEqualTo("M93RDOC6");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(5).getStringCellValue()).isEqualTo("M93RJCD6");
    }

    @Test
    public void should_create_deduction_file() throws IOException, InvalidFormatException {
        Policy policy1 = getValidatedPolicy(EVERY_MONTH);
        Policy policy2 = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = collectionFileRepository.save(
                collectionFile(
                        collectionFileLine(policy1, 100.0),
                        collectionFileLine(policy2, 150.0),
                        collectionFileLine(policy2, 200.0)
                ));
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        byte[] excelFileContent = rlsService.createDeductionExcelFile(updatedCollectionFile.getDeductionFile());

        Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelFileContent));
        assertThat(wb.getSheet("LFPATPTDR6")).isNotNull();
        assertThat(wb.getSheet("LFPATPTDR6").getLastRowNum()).isEqualTo(3);
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(0).getStringCellValue()).isEqualTo(policy1.getPolicyId());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(3).getStringCellValue()).isEqualTo("100.0");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(4).getStringCellValue()).isEqualTo(now().toString());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(1).getCell(5).getStringCellValue()).isEqualTo("");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(0).getStringCellValue()).isEqualTo(policy2.getPolicyId());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(3).getStringCellValue()).isEqualTo("150.0");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(4).getStringCellValue()).isEqualTo(now().toString());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(2).getCell(5).getStringCellValue()).isEqualTo("");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(0).getStringCellValue()).isEqualTo(policy2.getPolicyId());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(3).getStringCellValue()).isEqualTo("200.0");
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(4).getStringCellValue()).isEqualTo(now().toString());
        assertThat(wb.getSheet("LFPATPTDR6").getRow(3).getCell(5).getStringCellValue()).isEqualTo("");
    }

    private CollectionFile collectionFile(CollectionFileLine ... collectionFileLines) {
        CollectionFile collectionFile = new CollectionFile();
        collectionFile.setReceivedDate(LocalDateTime.now());
        for (CollectionFileLine collectionFileLine : collectionFileLines) {
            collectionFile.addLine(collectionFileLine);
        }
        return collectionFile;
    }

    private static CollectionFileLine collectionFileLine(Policy policy, Double amount) {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPaymentMode("myPaymentMode");
        collectionFileLine.setBankCode("myBankCode");
        collectionFileLine.setCollectionBank("collectionBank");
        collectionFileLine.setCollectionDate(now().toString());
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(amount);
        return collectionFileLine;
    }

    private Policy getValidatedPolicy(PeriodicityCode periodicityCode) {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(periodicityCode, 1000000.0, 5));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        Policy policy = policyService.createPolicy(quote);
        policy.setStatus(VALIDATED);
        policyRepository.save(policy);

        return policy;
    }
}
