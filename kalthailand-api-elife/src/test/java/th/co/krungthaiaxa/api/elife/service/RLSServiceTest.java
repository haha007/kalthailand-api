package th.co.krungthaiaxa.api.elife.service;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.data.DeductionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.api.elife.service.RLSService.ERROR_NO_REGISTRATION_KEY_FOUND;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RLSServiceTest extends ELifeTest {
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
    private LineService lineService;
    @Inject
    private MongoTemplate mongoTemplate;

    @Before
    public void setup() {
        lineService = mock(LineService.class);
        rlsService.setLineService(lineService);
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(CollectionFile.class);
    }

    @Test
    public void should_not_save_collection_file_when_there_is_an_error() {
        assertThatThrownBy(() -> rlsService.readCollectionExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(collectionFileRepository.findAll()).hasSize(0);
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
        LocalDateTime now = now();
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
    public void should_add_a_payment_with_null_registration_key() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setRegistrationKey(null));
        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());

        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        assertThat(newPayment.get().getRegistrationKey()).isNull();
    }

    @Test
    public void should_add_a_payment_with_none_null_registration_key() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());

        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        assertThat(newPayment.get().getRegistrationKey()).isEqualTo("something");
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_has_already_been_done() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());

        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        Assertions.assertThat(newPayment.get().getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);
        assertThat(newPayment.get().getAmount().getValue()).isEqualTo(100.0);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_due_date_is_older_than_28_days() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setDueDate(LocalDate.now().minusDays(30)));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());

        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        Assertions.assertThat(newPayment.get().getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);
        assertThat(newPayment.get().getAmount().getValue()).isEqualTo(100.0);
    }

    @Test
    public void should_find_a_payment_for_the_policy() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        assertThat(collectionFileLine.getPaymentId()).isNotNull();
    }

    @Test
    public void should_create_a_deduction_file_line_with_error_when_no_registration_key() throws IOException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

        Policy policy = getValidatedPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setRegistrationKey(null));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        DeductionFile deductionFile = new DeductionFile();
        rlsService.processCollectionFileLine(deductionFile, collectionFileLine);

        assertThat(deductionFile.getLines()).hasSize(1);
        assertThat(deductionFile.getLines().get(0).getAmount()).isEqualTo(100.0);
        assertThat(deductionFile.getLines().get(0).getBankCode()).isEqualTo("myBankCode");
        assertThat(deductionFile.getLines().get(0).getPaymentMode()).isEqualTo("M");
        assertThat(deductionFile.getLines().get(0).getPolicyNumber()).isEqualTo(policy.getPolicyId());
        assertThat(deductionFile.getLines().get(0).getProcessDate()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(deductionFile.getLines().get(0).getRejectionCode()).isEqualTo(LineService.LINE_PAY_INTERNAL_ERROR);

        Payment payment = paymentRepository.findOne(collectionFileLine.getPaymentId());
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations().get(0).getAmount().getValue()).isEqualTo(100.0);
        assertThat(payment.getPaymentInformations().get(0).getAmount().getCurrencyCode()).isEqualTo("THB");
        Assertions.assertThat(payment.getPaymentInformations().get(0).getChannel()).isEqualTo(ChannelType.LINE);
        assertThat(payment.getPaymentInformations().get(0).getCreditCardName()).isNull();
        assertThat(payment.getPaymentInformations().get(0).getDate()).isEqualTo(LocalDate.now());
        assertThat(payment.getPaymentInformations().get(0).getMethod()).isNull();
        assertThat(payment.getPaymentInformations().get(0).getRejectionErrorCode()).isEqualTo(LineService.LINE_PAY_INTERNAL_ERROR);
        assertThat(payment.getPaymentInformations().get(0).getRejectionErrorMessage()).isEqualTo(ERROR_NO_REGISTRATION_KEY_FOUND);
    }
/*
    @Test
    public void should_create_a_deduction_file_line_with_success() throws IOException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

        Policy policy = getValidatedPolicy(EVERY_MONTH);
        CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
        rlsService.addPaymentId(collectionFileLine);
        DeductionFile deductionFile = new DeductionFile();
        rlsService.processCollectionFileLine(deductionFile, collectionFileLine);

        assertThat(deductionFile.getLines()).hasSize(1);
        assertThat(deductionFile.getLines().get(0).getAmount()).isEqualTo(100.0);
        assertThat(deductionFile.getLines().get(0).getBankCode()).isEqualTo("myBankCode");
        assertThat(deductionFile.getLines().get(0).getPaymentMode()).isEqualTo("M");
        assertThat(deductionFile.getLines().get(0).getPolicyNumber()).isEqualTo(policy.getPolicyId());
        assertThat(deductionFile.getLines().get(0).getProcessDate()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(deductionFile.getLines().get(0).getRejectionCode()).isEqualTo("0000");

        Payment payment = paymentRepository.findOne(collectionFileLine.getPaymentId());
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations().get(0).getAmount().getValue()).isEqualTo(100.0);
        assertThat(payment.getPaymentInformations().get(0).getAmount().getCurrencyCode()).isEqualTo("THB");
        Assertions.assertThat(payment.getPaymentInformations().get(0).getChannel()).isEqualTo(ChannelType.LINE);
        assertThat(payment.getPaymentInformations().get(0).getCreditCardName()).isEqualTo("myCreditCardName");
        assertThat(payment.getPaymentInformations().get(0).getDate()).isEqualTo(LocalDate.now());
        assertThat(payment.getPaymentInformations().get(0).getMethod()).isEqualTo("myMethod");
        assertThat(payment.getPaymentInformations().get(0).getRejectionErrorCode()).isEqualTo("0000");
        assertThat(payment.getPaymentInformations().get(0).getRejectionErrorMessage()).isEqualTo("success");
    }
*/
    /*
    @Test
    public void should_mark_collection_file_as_processed() throws IOException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));
        Policy policy = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy, 100.0),
                collectionFileLine(policy, 150.0),
                collectionFileLine(policy, 200.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        assertThat(updatedCollectionFile.getJobStartedDate()).isAfter(updatedCollectionFile.getReceivedDate());
        assertThat(updatedCollectionFile.getJobEndedDate()).isAfter(updatedCollectionFile.getJobStartedDate());
    }
*/
    /*
    @Test
    public void should_process_collection_file() throws IOException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

        Policy policy1 = getValidatedPolicy(EVERY_MONTH);
        Policy policy2 = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy1, 100.0),
                collectionFileLine(policy2, 150.0),
                collectionFileLine(policy2, 200.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        List<DeductionFileLine> deductionFileLines = updatedCollectionFile.getDeductionFile().getLines();
        assertThat(deductionFileLines).extracting("policyNumber").containsExactly(policy1.getPolicyId(), policy2.getPolicyId(), policy2.getPolicyId());
        assertThat(deductionFileLines).extracting("bankCode").containsExactly("myBankCode", "myBankCode", "myBankCode");
        assertThat(deductionFileLines).extracting("paymentMode").containsExactly("M", "M", "M");
        assertThat(deductionFileLines).extracting("amount").containsExactly(100.0, 150.0, 200.0);
        assertThat(deductionFileLines).extracting("processDate").doesNotContainNull();
        assertThat(deductionFileLines).extracting("rejectionCode").containsExactly("0000", "0000", "0000");
    }
*/
    @Test
    public void should_not_create_deduction_file_line_when_no_deduction_line_created() {
        Policy policy = getValidatedPolicy(EVERY_MONTH);
        CollectionFile collectionFile = getValidatedCollectionFile(collectionFileLine(policy, 100.0));
        assertThatThrownBy(() -> rlsService.createDeductionExcelFile(collectionFile.getDeductionFile()))
                .isInstanceOf(IllegalArgumentException.class);
    }
/*
    @Test
    public void should_create_deduction_file_with_proper_header() throws IOException, InvalidFormatException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));
        Policy policy = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy, 100.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        byte[] excelFileContent = rlsService.createDeductionExcelFile(updatedCollectionFile.getDeductionFile());

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
    */
/*
    @Test
    public void should_create_deduction_file() throws IOException, InvalidFormatException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));
        Policy policy1 = getValidatedPolicy(EVERY_MONTH);
        Policy policy2 = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy1, 100.0),
                collectionFileLine(policy2, 150.0),
                collectionFileLine(policy2, 200.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        byte[] excelFileContent = rlsService.createDeductionExcelFile(updatedCollectionFile.getDeductionFile());

        Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelFileContent));
        assertThat(wb.getSheet("LFPATPTDR6")).isNotNull();
        assertThat(wb.getSheet("LFPATPTDR6").getLastRowNum()).isEqualTo(3);
        Row row1 = wb.getSheet("LFPATPTDR6").getRow(1);
        assertThat(row1.getCell(0).getStringCellValue()).isEqualTo(policy1.getPolicyId());
        assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(row1.getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(row1.getCell(3).getStringCellValue()).isEqualTo("100.0");
        assertThat(row1.getCell(4).getStringCellValue()).isNotNull();
        assertThat(row1.getCell(5).getStringCellValue()).isEqualTo("0000");
        Row row2 = wb.getSheet("LFPATPTDR6").getRow(2);
        assertThat(row2.getCell(0).getStringCellValue()).isEqualTo(policy2.getPolicyId());
        assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("150.0");
        assertThat(row2.getCell(4).getStringCellValue()).isNotNull();
        assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("0000");
        Row row3 = wb.getSheet("LFPATPTDR6").getRow(3);
        assertThat(row3.getCell(0).getStringCellValue()).isEqualTo(policy2.getPolicyId());
        assertThat(row3.getCell(1).getStringCellValue()).isEqualTo("myBankCode");
        assertThat(row3.getCell(2).getStringCellValue()).isEqualTo("M");
        assertThat(row3.getCell(3).getStringCellValue()).isEqualTo("200.0");
        assertThat(row3.getCell(4).getStringCellValue()).isNotNull();
        assertThat(row3.getCell(5).getStringCellValue()).isEqualTo("0000");
    }
*/
    private static CollectionFileLine collectionFileLine(Policy policy, Double amount) {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPaymentMode("myPaymentMode");
        collectionFileLine.setBankCode("myBankCode");
        collectionFileLine.setCollectionBank("collectionBank");
        collectionFileLine.setCollectionDate(LocalDate.now().toString());
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(amount);
        return collectionFileLine;
    }

    private CollectionFile getValidatedCollectionFile(CollectionFileLine... collectionFileLines) {
        CollectionFile collectionFile = new CollectionFile();
        collectionFile.setReceivedDate(now());
        for (CollectionFileLine collectionFileLine : collectionFileLines) {
            rlsService.addPaymentId(collectionFileLine);
            collectionFile.addLine(collectionFileLine);
        }

        return collectionFileRepository.save(collectionFile);
    }

    private Policy getValidatedPolicy(PeriodicityCode periodicityCode) {
        Quote quote = quoteService.createQuote(randomNumeric(20), ChannelType.LINE, TestUtil.productQuotation(periodicityCode, 1000000.0, 5));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateQuote(quote, "token");

        Policy policy = policyService.createPolicy(quote);
        policy.setStatus(PolicyStatus.VALIDATED);
        policy.getPayments().stream().forEach(payment -> payment.setRegistrationKey("something"));
        paymentRepository.save(policy.getPayments());

        return policyRepository.save(policy);
    }
}
